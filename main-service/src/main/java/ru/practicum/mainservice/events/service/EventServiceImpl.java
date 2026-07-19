package ru.practicum.mainservice.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.entity.Category;
import ru.practicum.common.entity.User;
import ru.practicum.common.enums.AdminStateAction;
import ru.practicum.common.enums.EventState;
import ru.practicum.common.enums.SortType;
import ru.practicum.common.exception.BadRequestException;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.mainservice.categories.service.CategoryService;
import ru.practicum.mainservice.events.dto.*;
import ru.practicum.mainservice.events.entity.Event;
import ru.practicum.mainservice.events.mapper.EventMapper;
import ru.practicum.mainservice.events.repository.EventRepository;
import ru.practicum.mainservice.users.service.UserService;
import ru.practicum.statistics.client.StatsClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private EventRepository repository;
    private EventMapper mapper;
    private StatsClient statsClient;
    private UserService userService;
    private CategoryService categoryService;

    private static final String APP_NAME = "ewm-service";

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pagebale = PageRequest.of(from / size, size);
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
            category = getCategoryEntity(request.getCategory());
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
        log.info("Getting events for user: userId={}", userId);

        getUserEntity(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> page = repository.findByInitiatorId(userId, pageable);

        return page.getContent().stream()
                .map(mapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        log.info("Creating event for user: userId={}, dto={}", userId, dto);

        User user = getUserEntity(userId);

        Category category = getCategoryEntity(dto.getCategory());

        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        Event event = mapper.toEntity(dto, category, user);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        event = repository.save(event);
        log.info("Created event with id: {}", event.getId());

        return mapper.toFullDto(event);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Getting event for user: userId={}, eventId={}", userId, eventId);

        getUserEntity(userId);

        Event event = getEventEntity(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id " + eventId + " not found for user " + userId);
        }

        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Updating event by user: userId={}, eventId={}, request={}", userId, eventId, request);

        getUserEntity(userId);

        Event event = getEventEntity(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id " + eventId + " not found for user " + userId);
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getEventDate() != null &&
                request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        Category category = null;

        if (request.getCategory() != null) {
            category = getCategoryEntity(request.getCategory());
        }

        mapper.updateFromUser(request, category, event);

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                default -> throw new BadRequestException("Unknown state action: " + request.getStateAction());
            }
        }

        event = repository.save(event);
        log.info("Updated event with id: {}", eventId);

        return mapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        log.info("Getting public events with filters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                        "onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        Pageable pageable;
        if (sort != null && sort.equalsIgnoreCase(SortType.VIEWS.name())) {
            pageable = PageRequest.of(from / size, size);
        } else {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        }

        Page<Event> page = repository.findPublishedEvents(text, categories, paid, rangeStart, rangeEnd, pageable);
        List<Event> events = page.getContent();

        if (onlyAvailable != null && onlyAvailable) {
            events = events.stream()
                    .filter(this::isEventAvailable)
                    .collect(Collectors.toList());
        }

        if (sort != null && sort.equalsIgnoreCase(SortType.VIEWS.name())) {
            events.sort(Comparator.comparingLong(this::getViewsCount));
        }

        List<EventShortDto> dtos = events.stream()
                .map(mapper::toShortDto)
                .collect(Collectors.toList());

        List<Event> finalEvents = events;
        dtos.forEach(dto -> {
            Event event = finalEvents.get(dtos.indexOf(dto));
            enrichShortDtoWithViewsAndRequests(dto, event);
        });

        statsClient.sendHit(new EndpointHit(APP_NAME, "/events", "0.0.0.0", LocalDateTime.now()));

        return dtos;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        log.info("Getting public event: eventId={}", eventId);

        Event event = getEventEntity(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }

        statsClient.sendHit(new EndpointHit(APP_NAME, "/events/" + eventId, "0.0.0.0", LocalDateTime.now()));

        return enrichWithViewsAndRequests(event);
    }

    @Override
    public Event getEventEntity(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));
    }

    @Override
    public boolean existsById(Long eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public List<Event> findAllByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(eventIds);
    }

    private EventFullDto enrichWithViewsAndRequests(Event event) {
        EventFullDto dto = mapper.toFullDto(event);
        dto.setConfirmedRequests(getConfirmedRequestsCount(event));
        dto.setViews(getViewsCount(event));
        return dto;
    }

    private void enrichShortDtoWithViewsAndRequests(EventShortDto dto, Event event) {
        dto.setConfirmedRequests(getConfirmedRequestsCount(event));
        dto.setViews(getViewsCount(event));
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

    private User getUserEntity(Long userId) {
        return userService.getUserEntity(userId);
    }

    private Category getCategoryEntity(Long categoryId) {
        return categoryService.getCategoryEntity(categoryId);
    }

    private boolean isEventAvailable(Event event) {
        if (event.getParticipantLimit() == 0) {
            return true;
        }
        return getConfirmedRequestsCount(event) < event.getParticipantLimit();
    }
}
