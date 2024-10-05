package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.exception.DataNotFoundException;
import ru.gpb.jpixelscoringbot.mapper.QuestionMapper;
import ru.gpb.jpixelscoringbot.model.Question;
import ru.gpb.jpixelscoringbot.model.QuestionType;
import ru.gpb.jpixelscoringbot.repository.QuestionRepository;
import ru.gpb.jpixelscoringbot.repository.QuestionTypeRepository;

import java.util.List;

import static ru.gpb.jpixelscoringbot.config.Constants.QUESTION_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class JavaQuestionService implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final QuestionTypeRepository questionTypeRepository;

    @Override
    public Question getReferenceById(Long id) {
        return questionRepository.getReferenceById(id);
    }

    @Override
    @Cacheable(cacheNames = QUESTION_CACHE_NAME)
    public List<QuestionDto> findQuestions() {
        var questionType = questionTypeRepository.findById(QuestionType.QuestionTypeCodeEnum.JAVA.getCode())
                .orElseThrow(() -> new DataNotFoundException("Не найдены вопросы по теме Java"));

        return questionRepository.findAllByQuestionType(questionType).stream()
                .map(questionMapper::toDto)
                .toList();
    }

    @Override
    public List<QuestionType> findQuestionTypes() {
        return questionTypeRepository.findAll();
    }
}
