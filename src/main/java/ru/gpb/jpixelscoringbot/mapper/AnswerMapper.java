package ru.gpb.jpixelscoringbot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gpb.jpixelscoringbot.dto.AnswerDto;
import ru.gpb.jpixelscoringbot.model.Answer;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "questionNumber", source = "question.questionNumber")
    AnswerDto toDto(Answer answer);
}
