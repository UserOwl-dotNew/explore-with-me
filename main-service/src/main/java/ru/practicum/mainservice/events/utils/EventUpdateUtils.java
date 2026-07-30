package ru.practicum.mainservice.events.utils;

import ru.practicum.common.entity.Category;
import ru.practicum.common.entity.Location;
import ru.practicum.mainservice.events.dto.UpdateEventAdminRequest;
import ru.practicum.mainservice.events.dto.UpdateEventUserRequest;
import ru.practicum.mainservice.events.entity.Event;

public class EventUpdateUtils {

    public static void updateFromAdmin(Event event, UpdateEventAdminRequest request, Category category) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (request.getLocation() != null) {
            event.setLocation(
                    new Location(
                            request.getLocation().getLat(),
                            request.getLocation().getLon()
                    )
            );
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
    }

    public static void updateFromUser(Event event, UpdateEventUserRequest request, Category category) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (request.getLocation() != null) {
            event.setLocation(
                    new Location(
                            request.getLocation().getLat(),
                            request.getLocation().getLon()
                    )
            );
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
    }
}