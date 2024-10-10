package ru.gpb.jpixelscoringbot.service;

import org.springframework.stereotype.Service;
import ru.gpb.jpixelscoringbot.config.SettingsProperties;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.mapper.QuestionMapper;
import ru.gpb.jpixelscoringbot.model.QuestionType;
import ru.gpb.jpixelscoringbot.repository.QuestionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JavaQuestionService extends BaseServiceQuestion {

    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final SettingsProperties settingsProperties;
    private final QuestionTypeService questionTypeService;

    public JavaQuestionService(QuestionMapper questionMapper,
                               QuestionRepository questionRepository,
                               SettingsProperties settingsProperties,
                               QuestionTypeService questionTypeService) {
        super(questionRepository);

        this.questionMapper = questionMapper;
        this.questionRepository = questionRepository;
        this.settingsProperties = settingsProperties;
        this.questionTypeService = questionTypeService;
    }

    @Override
    public List<QuestionDto> findAllOrderByRandom() {
        var questionType = questionTypeService.findById(getQuestionTypeCode());

        return questionRepository.findAllByQuestionTypeOrderByRandom(questionType.getCode(), getLimitSelectedRecordQuestions()).stream()
                .map(questionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public String getQuestionTypeCode() {
        return QuestionType.QuestionTypeCodeEnum.JAVA.getCode();
    }

    @Override
    public Integer getLimitSelectedRecordQuestions() {
        return settingsProperties.getNumberOfQuestionsAboutJava();
    }
}
