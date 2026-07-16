package ru.practicum.mainservice.users.mapper;

import org.mapstruct.Mapper;
import ru.practicum.common.dto.UserDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.common.entity.User;
import ru.practicum.mainservice.users.dto.NewUserRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    User toEntity(NewUserRequest dto);
}
