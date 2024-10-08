package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.gpb.jpixelscoringbot.model.Report;
import ru.gpb.jpixelscoringbot.repository.ReportRepository;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.gpb.jpixelscoringbot.config.Constants.REPORT_EMPTY;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String LINE_BREAK = "\n";
    private static final String RESULT_TEMPLATE = "Результат по специальности {0}: всего вопросов {1} из них {2} правильные";

    private final UserService userService;
    private final AnswerService answerService;
    private final List<QuestionService> questionServiceList;

    private final ReportRepository reportRepository;

    @Override
    @Transactional
    public void createAndSaveReport(Long telegramUserId, Long questionId, Long answerId) {
        var report = new Report();
        report.setUser(userService.getReferenceById(telegramUserId));
        report.setQuestion(questionServiceList.getFirst().getReferenceById(questionId));
        if (Objects.nonNull(answerId)) {
            report.setAnswer(answerService.getReferenceById(answerId));
        }
        report.setDateReport(LocalDate.now());
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void removePreviousReport(Long telegramUserId, String questionTypeCode) {
        reportRepository.deleteAllByUserTelegramIdAndQuestionQuestionTypeCode(telegramUserId, questionTypeCode);
    }

    @Override
    public String getResultReportByUser(Long telegramUserId) {
        List<Report> reports = reportRepository.findAllByUserTelegramId(telegramUserId);

        if (CollectionUtils.isEmpty(reports)) {
            return REPORT_EMPTY;
        }

        Map<String, List<Report>> reportMap = reports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getQuestion().getQuestionType().getCode(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        StringBuilder sb = new StringBuilder();

        reportMap.forEach((s, reportList) -> {
            Long totalCountQuestion = questionServiceList.stream()
                    .filter(questionService -> questionService.getQuestionTypeCode().equals(s))
                    .map(QuestionService::totalCountQuestionByQuestionTypeCode)
                    .findFirst()
                    .orElse(0L);

            Map<Boolean, List<Report>> resultMap = reportList.stream()
                    .collect(Collectors.partitioningBy(report -> Objects.nonNull(report.getAnswer()) && report.getAnswer().isRight()));

            sb.append(MessageFormat.format(RESULT_TEMPLATE, s, totalCountQuestion, resultMap.get(Boolean.TRUE).size()));
            sb.append(LINE_BREAK);
            sb.append(LINE_BREAK);
        });

        return sb.toString();
    }
}
