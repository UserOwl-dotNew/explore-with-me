package ru.practicum.mainservice.controller.comment;

import ru.practicum.common.dto.CommentDto;
import ru.practicum.mainservice.controller.api.comment.PublicCommentControllerApi;

import java.util.List;

public class PublicCommentController implements PublicCommentControllerApi {
    @Override
    public List<CommentDto> getComments(Long eventId, int from, int size, String sort) {
        return List.of();
    }

    @Override
    public CommentDto getComment(Long eventId, Long commentId) {
        return null;
    }
}
