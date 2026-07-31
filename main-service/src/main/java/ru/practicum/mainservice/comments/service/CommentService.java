package ru.practicum.mainservice.comments.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.common.dto.CommentDto;
import ru.practicum.common.dto.NewCommentDto;
import ru.practicum.common.dto.UpdateCommentDto;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.ForbiddenException;
import ru.practicum.common.exception.NotFoundException;

import java.util.List;

/**
 * Контракт сервиса комментариев
 */
public interface CommentService {

    /**
     * Создание нового комментария к событию
     *
     * @param userId  ID автора комментария
     * @param eventId ID события
     * @param dto     данные нового комментария
     * @return созданный комментарий
     * @throws NotFoundException если пользователь или событие не найдены
     * @throws ConflictException если событие не опубликовано
     * @throws ConflictException если пользователь не участвовал в событии (опционально)
     */
    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    /**
     * Обновление комментария
     *
     * @param userId    ID автора комментария
     * @param commentId ID комментария
     * @param dto       новые данные комментария
     * @return обновленный комментарий
     * @throws NotFoundException  если комментарий не найден
     * @throws ForbiddenException если пользователь не автор комментария
     * @throws ConflictException  если комментарий был удален
     */
    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    /**
     * Удаление комментария пользователем (soft delete)
     *
     * @param userId    ID пользователя
     * @param commentId ID комментария
     * @throws NotFoundException  если комментарий не найден
     * @throws ForbiddenException если пользователь не автор комментария
     */
    void deleteCommentByUser(Long userId, Long commentId);

    /**
     * Получение комментариев к событию (публичный доступ)
     *
     * @param eventId  ID события
     * @param pageable параметры пагинации
     * @return список комментариев
     * @throws NotFoundException если событие не найдено или не опубликовано
     */
    List<CommentDto> getEventComments(Long eventId, Pageable pageable);

    /**
     * Получение конкретного комментария (публичный доступ)
     *
     * @param commentId ID комментария
     * @return комментарий
     * @throws NotFoundException если комментарий не найден или удален
     */
    CommentDto getComment(Long commentId);

    /**
     * Получение всех комментариев пользователя (для администратора)
     *
     * @param userId   ID пользователя
     * @param pageable параметры пагинации
     * @return список комментариев (включая удаленные)
     * @throws NotFoundException если пользователь не найден
     */
    List<CommentDto> getUserCommentsByAdmin(Long userId, Pageable pageable);

    /**
     * Удаление комментария администратором (hard delete)
     *
     * @param commentId ID комментария
     * @throws NotFoundException если комментарий не найден
     */
    void deleteCommentByAdmin(Long commentId);

    /**
     * Получение всех комментариев события (для администратора)
     *
     * @param eventId  ID события
     * @param pageable параметры пагинации
     * @return список комментариев (включая удаленные)
     * @throws NotFoundException если событие не найдено
     */
    List<CommentDto> getEventCommentsByAdmin(Long eventId, Pageable pageable);
}