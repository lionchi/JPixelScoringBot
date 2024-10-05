package ru.gpb.jpixelscoringbot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.model.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "questionTypeCode", source = "questionType.code")
    @Mapping(target = "difficultyLevelCode", source = "difficultyLevel.code")
    QuestionDto toDto(Question dto);
}
