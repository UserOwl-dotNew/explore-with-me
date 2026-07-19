package ru.practicum.mainservice.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.events.dto.EventFullDto;
import ru.practicum.mainservice.events.dto.EventShortDto;
import ru.practicum.mainservice.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.common.config.JacksonConfig.DATE_TIME_FORMAT;

/**
 * Публичный контроллер для работы с событиями.
 * Предоставляет API для получения информации о событиях без авторизации.
 * <p>
 * Все эндпоинты этого контроллера доступны для неавторизованных пользователей
 * и возвращают только опубликованные события.
 * </p>
 *
 * @see EventService
 * @see EventFullDto
 * @see EventShortDto
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;

    /**
     * Получение событий с возможностью фильтрации
     * GET /events
     *
     * @param text          текст для поиска в аннотации и описании события (необязательный)
     * @param categories    список идентификаторов категорий для фильтрации (необязательный)
     * @param paid          фильтр по платности/бесплатности события (необязательный)
     * @param rangeStart    дата и время начала диапазона (необязательный) Формат: "yyyy-MM-dd HH:mm:ss"
     * @param rangeEnd      дата и время окончания диапазона (необязательный) Формат: "yyyy-MM-dd HH:mm:ss"
     * @param onlyAvailable фильтр для показа только событий с неисчерпанным лимитом запросов на участие
     * @param sort          вариант сортировки: {@code EVENT_DATE} - по дате события
     * @param from          {@code VIEWS} - по количеству просмотров (необязательный)
     * @param size          количество событий, которое нужно пропустить для пагинации
     * @return список кратких DTO событий {@link EventShortDto} с информацией о просмотрах и заявках
     * @throws ru.practicum.common.exception.BadRequestException если параметры запроса некорректны
     * @see EventShortDto
     */
    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /events with params: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                        "onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    /**
     * Получение подробной информации об опубликованном событии по его идентификатору
     * GET /events/{id}
     *
     * @param id Id события
     * @return Полная информация о событии {@link EventFullDto}
     * @throws ru.practicum.common.exception.NotFoundException   если событие с указанным ID не найдено или не опубликовано
     * @throws ru.practicum.common.exception.BadRequestException если идентификатор имеет некорректный формат
     * @see EventFullDto
     */
    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id) {
        log.info("GET /events/{}", id);
        return eventService.getPublicEvent(id);
    }
}
