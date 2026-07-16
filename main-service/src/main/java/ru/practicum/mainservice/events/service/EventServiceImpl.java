package ru.practicum.mainservice.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.entity.Category;
import ru.practicum.common.enums.AdminStateAction;
import ru.practicum.common.enums.EventState;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.dto.ViewStats;
import ru.practicum.mainservice.categories.repository.CategoryRepository;
import ru.practicum.mainservice.events.dto.*;
import ru.practicum.mainservice.events.entity.Event;
import ru.practicum.mainservice.events.mapper.EventMapper;
import ru.practicum.mainservice.events.repository.EventRepository;
import ru.practicum.statistics.client.StatsClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private EventRepository repository;
    private EventMapper mapper;
    private StatsClient statsClient;
    private CategoryRepository categoryRepository;

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pagebale = PageRequest.of(from /size, size); // TODO разобраться подробнее
        Page<Event> page = repository.findAllByAdminFilters(users, states, categories, rangeStart, rangeEnd, pagebale);
        return page.getContent().stream()
                .map(this::enrichWithViewsAndRequests)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {
        log.info("Updating event by admin: eventId={}, request={}", eventId, request);

        Event event = getEventEntity(eventId);

        if (request.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish the event because it's not in PENDING state");
            }

            if (request.getEventDate() != null &&
                    request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour from now");
            }
        }

        if (request.getStateAction() == AdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot reject published event");
            }
        }

        Category category = null;
        if (request.getCategory() != null) {
            category = categoryRepository.getReferenceById(request.getCategory());
        }

        mapper.updateFromAdmin(request, category, event);

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setEventDate(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        event = repository.save(event);
        return enrichWithViewsAndRequests(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        return List.of();
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        return null;
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        return null;
    }

    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        return null;
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        return List.of();
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        return null;
    }

    @Override
    public Event getEventEntity(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));
    }

    @Override
    public boolean existsById(Long eventId) {
        return false;
    }

    @Override
    public List<Event> findAllByIds(List<Long> eventIds) {
        return List.of();
    }

    private EventFullDto enrichWithViewsAndRequests(Event event) {
        EventFullDto dto = mapper.toFullDto(event);
        dto.setConfirmedRequests(getConfirmedRequestsCount(event));
        dto.setViews(getViewsCount(event));
        return dto;
    }

    private long getConfirmedRequestsCount(Event event) {
        // TODO: Будет реализовано после интеграции с requests
        return 0L;
    }

    private long getViewsCount(Event event) {
        try {
            List<ViewStats> stats = statsClient.getStats(
                    String.valueOf(event.getPublishedOn() != null ? event.getPublishedOn() : event.getCreatedOn()),
                    String.valueOf(LocalDateTime.now()),
                    List.of("/events/" + event.getId()),
                    true
            );

            return stats.isEmpty() ? 0 : stats.getFirst().getHits();
        } catch (Exception e) {
            log.warn("Failed to get views for event {}", event.getId(), e);
            return 0;
        }
    }
}
