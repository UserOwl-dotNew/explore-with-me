package ru.practicum.mainservice.service;

import ru.practicum.mainservice.events.entity.Event;

public interface EventService {
    Event getEventById(Long eventId);
}
