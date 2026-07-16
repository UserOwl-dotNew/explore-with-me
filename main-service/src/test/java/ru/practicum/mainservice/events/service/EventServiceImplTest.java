package ru.practicum.mainservice.events.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.LocationDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.common.entity.Category;
import ru.practicum.common.entity.Location;
import ru.practicum.common.entity.User;
import ru.practicum.common.enums.AdminStateAction;
import ru.practicum.common.enums.EventState;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.mainservice.categories.repository.CategoryRepository;
import ru.practicum.mainservice.events.dto.EventFullDto;
import ru.practicum.mainservice.events.dto.UpdateEventAdminRequest;
import ru.practicum.mainservice.events.entity.Event;
import ru.practicum.mainservice.events.mapper.EventMapper;
import ru.practicum.mainservice.events.repository.EventRepository;
import ru.practicum.statistics.client.StatsClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private StatsClient statsClient;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private EventFullDto eventFullDto;
    private Category category;
    private User initiator;
    private UpdateEventAdminRequest updateRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        category = new Category();
        category.setId(1L);
        category.setName("Концерты");

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());

        initiator = new User();
        initiator.setId(1L);
        initiator.setName("Иван Петров");
        initiator.setEmail("ivan@mail.ru");

        UserShortDto initiatorDto = new UserShortDto();
        initiatorDto.setId(initiator.getId());
        initiatorDto.setName(initiator.getName());

        Location location = new Location();
        location.setLat(55.754167f);
        location.setLon(37.62f);

        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());

        event = new Event();
        event.setId(1L);
        event.setAnnotation("Новая аннотация для тестового события");
        event.setDescription("Новое описание для проверки работы метода");
        event.setTitle("Тестовое событие");
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setLocation(location);
        event.setEventDate(now.plusDays(5));
        event.setCreatedOn(now);
        event.setPublishedOn(null);
        event.setState(EventState.PENDING);
        event.setPaid(false);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);

        eventFullDto = EventFullDto.builder()
                .id(1L)
                .annotation("Новая аннотация для тестового события")
                .description("Новое описание для проверки работы метода")
                .title("Тестовое событие")
                .category(categoryDto)
                .initiator(initiatorDto)
                .location(locationDto)
                .eventDate(now.plusDays(5))
                .createdOn(now)
                .state(EventState.PENDING)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .confirmedRequests(0L)
                .views(0L)
                .build();

        updateRequest = UpdateEventAdminRequest.builder()
                .annotation("Обновленная аннотация")
                .description("Обновленное описание")
                .title("Обновленное название")
                .category(1L)
                .eventDate(now.plusDays(10))
                .paid(true)
                .participantLimit(20)
                .requestModeration(false)
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();
    }

    @Test
    void getAdminEvents_shouldReturnListOfEvents() {
        Page<Event> eventPage = new PageImpl<>(List.of(event));
        List<EventFullDto> expectedDtos = List.of(eventFullDto);

        when(eventRepository.findAllByAdminFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)
        )).thenReturn(eventPage);
        when(eventMapper.toFullDto(event)).thenReturn(eventFullDto);

        List<EventFullDto> result = eventService.getAdminEvents(
                null, null, null, null, null, 0, 10
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAnnotation()).isEqualTo("Новая аннотация для тестового события");
    }

    @Test
    void getAdminEvents_withFilters_shouldReturnFilteredEvents() {
        List<Long> users = List.of(1L, 2L);
        List<EventState> states = List.of(EventState.PENDING);
        List<Long> categories = List.of(1L);
        LocalDateTime rangeStart = now.minusDays(1);
        LocalDateTime rangeEnd = now.plusDays(10);

        Page<Event> eventPage = new PageImpl<>(List.of(event));

        when(eventRepository.findAllByAdminFilters(
                eq(users), eq(states), eq(categories), eq(rangeStart), eq(rangeEnd), any(PageRequest.class)
        )).thenReturn(eventPage);
        when(eventMapper.toFullDto(event)).thenReturn(eventFullDto);

        List<EventFullDto> result = eventService.getAdminEvents(
                users, states, categories, rangeStart, rangeEnd, 0, 10
        );

        assertThat(result).hasSize(1);
        verify(eventRepository).findAllByAdminFilters(
                eq(users), eq(states), eq(categories), eq(rangeStart), eq(rangeEnd), any(PageRequest.class)
        );
    }

    @Test
    void getAdminEvents_shouldHandleEmptyResult() {
        Page<Event> emptyPage = new PageImpl<>(List.of());

        when(eventRepository.findAllByAdminFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)
        )).thenReturn(emptyPage);

        List<EventFullDto> result = eventService.getAdminEvents(
                null, null, null, null, null, 0, 10
        );

        assertThat(result).isEmpty();
    }

    @Test
    void updateAdminEvent_shouldPublishEventSuccessfully() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.getReferenceById(1L)).thenReturn(category);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toFullDto(event)).thenReturn(eventFullDto);

        doAnswer(invocation -> {
            UpdateEventAdminRequest request = invocation.getArgument(0);
            Category cat = invocation.getArgument(1);
            Event eventToUpdate = invocation.getArgument(2);
            eventToUpdate.setAnnotation(request.getAnnotation());
            eventToUpdate.setDescription(request.getDescription());
            eventToUpdate.setTitle(request.getTitle());
            eventToUpdate.setCategory(cat);
            eventToUpdate.setEventDate(request.getEventDate());
            eventToUpdate.setPaid(request.getPaid());
            eventToUpdate.setParticipantLimit(request.getParticipantLimit());
            eventToUpdate.setRequestModeration(request.getRequestModeration());
            return null;
        }).when(eventMapper).updateFromAdmin(any(UpdateEventAdminRequest.class), any(Category.class), any(Event.class));

        EventFullDto result = eventService.updateAdminEvent(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(eventRepository).save(event);
    }

    @Test
    void updateAdminEvent_shouldThrowNotFoundException_whenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateAdminEvent(999L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event with id 999 was not found");
    }

    @Test
    void updateAdminEvent_shouldThrowConflictException_whenPublishingNonPendingEvent() {
        event.setState(EventState.CANCELED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateAdminEvent(1L, updateRequest))
                .isInstanceOf(ConflictException.class).
                hasMessageContaining("Cannot publish the event because it's not in PENDING state");
    }

    @Test
    void updateAdminEvent_shouldThrowConflictException_whenEventDateTooSoon() {
        updateRequest.setEventDate(now.plusMinutes(30));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateAdminEvent(1L, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Event date must be at least 1 hour from now");
    }

    @Test
    void updateAdminEvent_shouldThrowConflictException_whenRejectingPublishedEvent() {
        event.setState(EventState.PUBLISHED);
        updateRequest.setStateAction(AdminStateAction.REJECT_EVENT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateAdminEvent(1L, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot reject published event");
    }

    @Test
    void updateAdminEvent_shouldRejectEventSuccessfully() {
        updateRequest.setStateAction(AdminStateAction.REJECT_EVENT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.getReferenceById(1L)).thenReturn(category);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toFullDto(event)).thenReturn(eventFullDto);

        doAnswer(invocation -> null)
                .when(eventMapper).updateFromAdmin(any(UpdateEventAdminRequest.class), any(Category.class), any(Event.class));

        EventFullDto result = eventService.updateAdminEvent(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(event.getState()).isEqualTo(EventState.CANCELED);
        verify(eventRepository).save(event);
    }

    @Test
    void updateAdminEvent_shouldUpdateWithoutStateAction() {
        String newAnnotation = "Новая аннотация для тестового события";
        String newDescription = "Новое описание для проверки работы метода";

        UpdateEventAdminRequest requestWithoutState = UpdateEventAdminRequest.builder()
                .annotation(newAnnotation)
                .description(newDescription)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toFullDto(event)).thenReturn(eventFullDto);

        lenient().doAnswer(invocation -> {
            UpdateEventAdminRequest req = invocation.getArgument(0);
            Event eventToUpdate = invocation.getArgument(2);

            if (req.getAnnotation() != null) {
                eventToUpdate.setAnnotation(req.getAnnotation());
            }
            if (req.getDescription() != null) {
                eventToUpdate.setDescription(req.getDescription());
            }
            return null;
        }).when(eventMapper).updateFromAdmin(
                any(UpdateEventAdminRequest.class),
                any(Category.class),
                any(Event.class)
        );

        EventFullDto result = eventService.updateAdminEvent(1L, requestWithoutState);

        assertThat(result).isNotNull();
        assertThat(event.getState()).isEqualTo(EventState.PENDING);
        assertThat(event.getAnnotation()).isEqualTo(newAnnotation);
        assertThat(event.getDescription()).isEqualTo(newDescription);
        verify(eventRepository).save(event);
    }

    @Test
    void getEventEntity_shouldReturnEvent_whenExists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.getEventEntity(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getEventEntity_shouldThrowNotFoundException_whenNotExists() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventEntity(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event with id 999 was not found");
    }
}
