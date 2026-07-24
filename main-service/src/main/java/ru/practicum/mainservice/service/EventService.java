package ru.practicum.mainservice.service;

import ru.practicum.mainservice.model.Event;

public interface EventService {
    Event getEventById(Long eventId);
}
