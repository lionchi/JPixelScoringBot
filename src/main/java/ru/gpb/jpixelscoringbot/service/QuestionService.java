package ru.gpb.jpixelscoringbot.service;

import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.model.Question;

import java.util.List;

public interface QuestionService {

    Question getReferenceById(Long id);

    List<QuestionDto> findQuestions();

    Long totalCountQuestionByQuestionTypeCode();

    String getQuestionTypeCode();
}
