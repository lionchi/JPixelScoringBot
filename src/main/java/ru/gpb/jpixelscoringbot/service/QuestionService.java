package ru.gpb.jpixelscoringbot.service;

import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.model.Question;
import ru.gpb.jpixelscoringbot.model.QuestionType;

import java.util.List;

public interface QuestionService {

    Question getReferenceById(Long id);

    List<QuestionDto> findQuestions();

    List<QuestionType> findQuestionTypes();
}
