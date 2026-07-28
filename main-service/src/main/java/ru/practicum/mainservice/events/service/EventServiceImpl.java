package ru.practicum.mainservice.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.dto.EventShortDto;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository repository;
    private final EventMapper mapper;
    private final StatsClient statsClient;
    private final UserService userService;
    private final CategoryService categoryService;

    private static final String APP_NAME = "ewm-service";

    @Override
    public List<EventFullDto> getAdminEvents(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);

        Specification<Event> specification = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            root.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            root.get("state").in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            root.get("category").get("id").in(categories)
            );
        }

        if (rangeStart != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.get("eventDate"), rangeEnd));
        }

        Page<Event> page = repository.findAll(
                specification,
                pageable
        );

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
                    event.setPublishedOn(LocalDateTime.now());
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
    @Transactional
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        log.info("Getting public events with filters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                        "onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        LocalDateTime finalRangeStart = rangeStart == null ? LocalDateTime.now() : rangeStart;

        if (rangeEnd != null && finalRangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException(
                    "Range start must be before range end"
            );
        }

        Pageable pageable;

        if (SortType.VIEWS.name().equalsIgnoreCase(sort)) {
            pageable = PageRequest.of(
                    from / size,
                    size
            );
        } else {
            pageable = PageRequest.of(
                    from / size,
                    size,
                    Sort.by("eventDate").ascending()
            );
        }

        Specification<Event> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED);

        if (text != null && !text.isBlank()) {
            String pattern = "%" + text.toLowerCase(Locale.ROOT) + "%";

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.or(
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                                            pattern
                                    ),
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                            pattern
                                    )
                            )
            );
        }

        if (categories != null && !categories.isEmpty()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            root.get("category").get("id").in(categories)
            );
        }

        if (paid != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("paid"), paid)
            );
        }

        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), finalRangeStart)
        );

        if (rangeEnd != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.get("eventDate"),
                                    rangeEnd
                            )
            );
        }

        Page<Event> eventPage = repository.findAll(
                specification,
                pageable
        );

        List<Event> events = new ArrayList<>(
                eventPage.getContent()
        );

        if (onlyAvailable) {
            events = events.stream()
                    .filter(this::isEventAvailable)
                    .collect(Collectors.toList());
        }

        if (SortType.VIEWS.name().equalsIgnoreCase(sort)) {
            events.sort(
                    Comparator.comparingLong(
                            this::getViewsCount
                    ).reversed()
            );
        }

        List<EventShortDto> result = new ArrayList<>();

        for (Event event : events) {
            EventShortDto dto = mapper.toShortDto(event);

            enrichShortDtoWithViewsAndRequests(
                    dto,
                    event
            );

            result.add(dto);
        }

        statsClient.sendHit(
                new EndpointHit(
                        APP_NAME,
                        "/events",
                        "0.0.0.0",
                        LocalDateTime.now()
                )
        );

        return result;
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
