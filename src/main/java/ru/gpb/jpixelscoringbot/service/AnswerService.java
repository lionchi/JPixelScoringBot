package ru.gpb.jpixelscoringbot.service;

import ru.gpb.jpixelscoringbot.dto.AnswerDto;
import ru.gpb.jpixelscoringbot.model.Answer;

import java.util.List;

public interface AnswerService {

    Answer getReferenceById(Long id);

    AnswerDto findById(Long id);

    List<AnswerDto> findByQuestionId(Long questionId);
}
