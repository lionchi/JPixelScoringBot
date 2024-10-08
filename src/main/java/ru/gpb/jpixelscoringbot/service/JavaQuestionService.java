package ru.gpb.jpixelscoringbot.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.mapper.QuestionMapper;
import ru.gpb.jpixelscoringbot.model.QuestionType;
import ru.gpb.jpixelscoringbot.repository.QuestionRepository;

import java.util.List;

import static ru.gpb.jpixelscoringbot.config.Constants.QUESTION_CACHE_NAME;

@Service
public class JavaQuestionService extends BaseServiceQuestion {

    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final QuestionTypeService questionTypeService;

    public JavaQuestionService(QuestionMapper questionMapper, QuestionRepository questionRepository, QuestionTypeService questionTypeService) {
        super(questionRepository);

        this.questionMapper = questionMapper;
        this.questionRepository = questionRepository;
        this.questionTypeService = questionTypeService;
    }

    @Override
    @Cacheable(cacheNames = QUESTION_CACHE_NAME)
    public List<QuestionDto> findQuestions() {
        var questionType = questionTypeService.findById(getQuestionTypeCode());

        return questionRepository.findAllByQuestionType(questionType).stream()
                .map(questionMapper::toDto)
                .toList();
    }

    @Override
    public Long totalCountQuestionByQuestionTypeCode() {
        return questionRepository.countAllByQuestionType(questionTypeService.findById(getQuestionTypeCode()));
    }

    @Override
    public String getQuestionTypeCode() {
        return QuestionType.QuestionTypeCodeEnum.JAVA.getCode();
    }
}
