package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.gpb.jpixelscoringbot.dto.AnswerDto;
import ru.gpb.jpixelscoringbot.exception.DataNotFoundException;
import ru.gpb.jpixelscoringbot.mapper.AnswerMapper;
import ru.gpb.jpixelscoringbot.model.Answer;
import ru.gpb.jpixelscoringbot.repository.AnswerRepository;

import java.util.List;

import static ru.gpb.jpixelscoringbot.config.Constants.ANSWER_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerMapper answerMapper;
    private final AnswerRepository answerRepository;

    @Override
    public Answer getReferenceById(Long id) {
        return answerRepository.getReferenceById(id);
    }

    @Override
    @Cacheable(cacheNames = ANSWER_CACHE_NAME, key = "#id")
    public AnswerDto findById(Long id) {
        return answerRepository.findById(id)
                .map(answerMapper::toDto)
                .orElseThrow(() -> new DataNotFoundException("Не смог найти ответ по id " + id));
    }

    @Override
    @Cacheable(cacheNames = ANSWER_CACHE_NAME, key = "#questionId")
    public List<AnswerDto> findByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .map(answerMapper::toDto)
                .toList();
    }
}
