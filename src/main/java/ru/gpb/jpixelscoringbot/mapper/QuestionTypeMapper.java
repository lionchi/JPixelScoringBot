package ru.gpb.jpixelscoringbot.mapper;

import org.mapstruct.Mapper;
import ru.gpb.jpixelscoringbot.dto.QuestionTypeDto;
import ru.gpb.jpixelscoringbot.model.QuestionType;

@Mapper(componentModel = "spring")
public interface QuestionTypeMapper {

    QuestionTypeDto toDto(QuestionType questionType);
}
