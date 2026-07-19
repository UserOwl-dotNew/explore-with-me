package ru.practicum.mainservice.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.events.dto.EventFullDto;
import ru.practicum.mainservice.events.dto.EventShortDto;
import ru.practicum.mainservice.events.dto.NewEventDto;
import ru.practicum.mainservice.events.dto.UpdateEventUserRequest;
import ru.practicum.mainservice.events.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    /**
     * Получение событий, добавленных текущим пользователем
     * GET /users/{userId}/events
     *
     * @param userId Id пользователя
     * @param from   С какого события показать информацию
     * @param size   Сколько событий показать за раз
     * @return Список событий
     */
    @GetMapping
    public List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /users/{}/events with from={}, size={}", userId, from, size);
        return eventService.getUserEvents(userId, from, size);
    }

    /**
     * Добавление нового события
     * POST /users/{userId}/events
     *
     * @param userId Id пользователя
     * @param dto    Сущность нового события
     * @return Полная информация о событии
     */
    @PostMapping
    public EventFullDto createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto dto) {

        log.info("POST /users/{}/events with request: {}", userId, dto);
        return eventService.createEvent(userId, dto);
    }

    /**
     * Получение полной информации о событии добавленном текущим пользователем
     * GET /users/{userId}/events/{eventId}
     *
     * @param userId  Id пользователя
     * @param eventId Id события
     * @return Полная информация о событии
     */
    @GetMapping("/{eventId}")
    public EventFullDto getEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getUserEvent(userId, eventId);
    }

    /**
     * Изменение события добавленного текущим пользователем
     * PATCH /users/{userId}/events/{eventId}
     *
     * @param userId  Id пользователя
     * @param eventId Id события
     * @param request Запрос с измененными данными для события
     * @return Полная информация о событии
     */
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest request) {

        log.info("PATCH /users/{}/events/{} with request: {}", userId, eventId, request);
        return eventService.updateUserEvent(userId, eventId, request);
    }
}
