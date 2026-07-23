package ru.practicum.mainservice.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.common.entity.Category;
import ru.practicum.common.entity.User;
import ru.practicum.mainservice.events.dto.*;
import ru.practicum.mainservice.events.entity.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventFullDto toFullDto(Event event);

    EventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    Event toEntity(NewEventDto dto, Category category, User initiator);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "dto.location")
    void updateFromAdmin(UpdateEventAdminRequest dto, Category category, @MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "dto.location")
    void updateFromUser(UpdateEventUserRequest dto, Category category, @MappingTarget Event event);
}
