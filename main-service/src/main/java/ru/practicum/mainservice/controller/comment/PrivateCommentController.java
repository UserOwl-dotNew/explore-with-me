package ru.practicum.mainservice.controller.comment;

import ru.practicum.common.dto.CommentDto;
import ru.practicum.common.dto.NewCommentDto;
import ru.practicum.common.dto.UpdateCommentDto;
import ru.practicum.mainservice.controller.api.comment.PrivateCommentControllerApi;

public class PrivateCommentController implements PrivateCommentControllerApi {
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto request) {
        return null;
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto request) {
        return null;
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {

    }
}
