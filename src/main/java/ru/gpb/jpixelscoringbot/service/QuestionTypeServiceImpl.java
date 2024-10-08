package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.gpb.jpixelscoringbot.dto.QuestionTypeDto;
import ru.gpb.jpixelscoringbot.exception.DataNotFoundException;
import ru.gpb.jpixelscoringbot.mapper.QuestionTypeMapper;
import ru.gpb.jpixelscoringbot.model.QuestionType;
import ru.gpb.jpixelscoringbot.repository.QuestionTypeRepository;

import java.util.List;

import static ru.gpb.jpixelscoringbot.config.Constants.QUESTION_TYPE_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class QuestionTypeServiceImpl implements QuestionTypeService {

    private final QuestionTypeMapper questionTypeMapper;
    private final QuestionTypeRepository questionTypeRepository;

    @Override
    @Cacheable(cacheNames = QUESTION_TYPE_CACHE_NAME)
    public QuestionType findById(String code) {
        return questionTypeRepository.findById(code)
                .orElseThrow(() -> new DataNotFoundException("Не найдена тема по code " + code));
    }

    @Override
    @Cacheable(cacheNames = QUESTION_TYPE_CACHE_NAME)
    public List<QuestionTypeDto> findQuestionTypes() {
        return questionTypeRepository.findAll().stream()
                .map(questionTypeMapper::toDto)
                .toList();
    }
}
