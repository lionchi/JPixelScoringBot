package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gpb.jpixelscoringbot.model.Report;
import ru.gpb.jpixelscoringbot.repository.ReportRepository;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserService userService;
    private final AnswerService answerService;
    private final QuestionService questionService;

    private final ReportRepository reportRepository;

    @Override
    @Transactional
    public void createAndSaveReport(Long telegramUserId, Long questionId, Long answerId) {
        var report = new Report();
        report.setUser(userService.getReferenceById(telegramUserId));
        report.setQuestion(questionService.getReferenceById(questionId));
        if (Objects.nonNull(answerId)) {
            report.setAnswer(answerService.getReferenceById(answerId));
        }
        report.setDateReport(LocalDate.now());
        reportRepository.save(report);
    }
}
