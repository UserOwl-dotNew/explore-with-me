package ru.practicum.mainservice.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.common.dto.UserDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.common.entity.User;
import ru.practicum.mainservice.users.dto.NewUserRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "name", source = "name")
    UserDto toDto(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    UserShortDto toShortDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "name", source = "name")
    User toEntity(NewUserRequest dto);
}
