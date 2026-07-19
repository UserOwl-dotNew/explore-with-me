package ru.practicum.mainservice.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.practicum.common.entity.Category;
import ru.practicum.common.entity.User;
import ru.practicum.mainservice.events.dto.*;
import ru.practicum.mainservice.events.entity.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventFullDto toFullDto(Event event);

    EventShortDto toShortDto(Event event);

    Event toEntity(NewEventDto dto, Category category, User initiator);

    void updateFromAdmin(UpdateEventAdminRequest dto, Category category, @MappingTarget Event event);

    void updateFromUser(UpdateEventUserRequest dto, Category category, @MappingTarget Event event);
}
