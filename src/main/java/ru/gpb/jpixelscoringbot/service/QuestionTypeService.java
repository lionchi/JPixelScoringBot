package ru.gpb.jpixelscoringbot.service;

import ru.gpb.jpixelscoringbot.dto.QuestionTypeDto;
import ru.gpb.jpixelscoringbot.model.QuestionType;

import java.util.List;

public interface QuestionTypeService {

    QuestionType findById(String code);

    List<QuestionTypeDto> findQuestionTypes();
}
