package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mainservice.events.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
