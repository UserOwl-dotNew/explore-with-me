package ru.practicum.mainservice.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.enums.EventState;
import ru.practicum.mainservice.events.dto.EventFullDto;
import ru.practicum.mainservice.events.dto.UpdateEventAdminRequest;
import ru.practicum.mainservice.events.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static ru.practicum.common.config.JacksonConfig.DATE_TIME_FORMAT;

/**
 * Контроллер для административного управления событиями.
 * Предоставляет эндпоинты для поиска и модерации событий.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService service;

    /**
     * Получение списка событий с фильтрацией.
     * Возвращает полную информацию обо всех событиях, соответствующих переданным условиям.
     * Если событий не найдено - возвращает пустой список.
     *
     * @param users       список ID пользователей, чьи события нужно найти (опционально)
     * @param states      список состояний событий (опционально)
     * @param categories  список ID категорий (опционально)
     * @param rangeStart  дата и время, не раньше которых должно произойти событие (опционально)
     * @param rangeEnd    дата и время, не позже которых должно произойти событие (опционально)
     * @param from        количество событий для пропуска (пагинация)
     * @param size        количество событий в наборе (пагинация)
     * @return список событий с полной информацией
     */
    @GetMapping
    public Collection<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("GET /admin/events with params: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return service.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    /**
     * Редактирование события администратором.
     * Позволяет изменять любые данные события, включая статус (публикация/отклонение).
     * Валидация данных не требуется.
     *
     * @param eventId ID редактируемого события
     * @param request данные для обновления события
     * @return обновленное событие с полной информацией
     * @throws ru.practicum.common.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.common.exception.ConflictException если:
     *         - дата начала события раньше чем через час от даты публикации
     *         - попытка опубликовать событие не в состоянии PENDING
     *         - попытка отклонить уже опубликованное событие
     */
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest request
    ) {
        log.info("PATCH /admin/events/{} with request: {}", eventId, request);
        return service.updateAdminEvent(eventId, request);
    }
}
