package ru.practicum.mainservice.controller.comment;

import ru.practicum.common.dto.CommentDto;
import ru.practicum.mainservice.controller.api.comment.AdminCommentControllerApi;

import java.util.List;

public class AdminCommentController implements AdminCommentControllerApi {

    @Override
    public List<CommentDto> getUserComments(Long userId, int from, int size, String sort) {
        return List.of();
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, int from, int size, String sort) {
        return List.of();
    }

    @Override
    public void deleteComment(Long commentId) {

    }
}
