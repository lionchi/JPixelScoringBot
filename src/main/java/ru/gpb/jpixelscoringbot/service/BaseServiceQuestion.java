package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import ru.gpb.jpixelscoringbot.model.Question;
import ru.gpb.jpixelscoringbot.repository.QuestionRepository;

@RequiredArgsConstructor
public abstract class BaseServiceQuestion implements QuestionService {

    public final QuestionRepository questionRepository;

    @Override
    public Question getReferenceById(Long id) {
        return questionRepository.getReferenceById(id);
    }
}
