package ru.gpb.jpixelscoringbot.mapper;

import org.mapstruct.Mapper;
import ru.gpb.jpixelscoringbot.dto.UserDto;
import ru.gpb.jpixelscoringbot.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
}
