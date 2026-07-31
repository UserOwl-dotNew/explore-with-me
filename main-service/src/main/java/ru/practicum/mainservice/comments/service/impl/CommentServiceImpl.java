package ru.practicum.mainservice.comments.service.impl;

import org.springframework.stereotype.Service;
import ru.practicum.common.dto.CommentDto;
import ru.practicum.common.dto.NewCommentDto;
import ru.practicum.common.dto.UpdateCommentDto;
import ru.practicum.mainservice.comments.service.CommentService;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        return null;
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        return null;
    }

    @Override
    public void deleteCommentByUser(Long userId, Long commentId) {

    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, int from, int size, String sort) {
        return List.of();
    }

    @Override
    public CommentDto getComment(Long eventId, Long commentId) {
        return null;
    }

    @Override
    public List<CommentDto> getUserCommentsByAdmin(Long userId, int from, int size, String sort) {
        return List.of();
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {

    }

    @Override
    public List<CommentDto> getEventCommentsByAdmin(Long eventId, int from, int size, String sort) {
        return List.of();
    }
}
