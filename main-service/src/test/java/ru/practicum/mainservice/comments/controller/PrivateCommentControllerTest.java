package ru.practicum.mainservice.comments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.dto.CommentDto;
import ru.practicum.common.dto.NewCommentDto;
import ru.practicum.common.dto.UpdateCommentDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.mainservice.comments.service.CommentService;
import ru.practicum.mainservice.controller.comment.PrivateCommentController;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateCommentController.class)
public class PrivateCommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService service;

    private final Long userId = 1L;
    private final Long eventId = 1L;
    private final Long commentId = 1L;

    @Test
    void createComment_shouldReturnCreatedComment() throws Exception {
        NewCommentDto request = NewCommentDto.builder()
                .text("Great event!")
                .build();

        when(service.createComment(eq(userId), eq(eventId), any(NewCommentDto.class)))
                .thenReturn(createCommentDto());

        mvc.perform(post("/users/{userId}/comments/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great event!"))
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.author.id").value(userId));
    }

    @Test
    void createComment_withEmptyText_shouldReturnBadRequest() throws Exception {
        NewCommentDto request = NewCommentDto.builder()
                .text("")
                .build();

        mvc.perform(post("/users/{userId}/comments/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_withBlankText_shouldReturnBadRequest() throws Exception {
        NewCommentDto request = NewCommentDto.builder()
                .text("   ")
                .build();

        mvc.perform(post("/users/{userId}/comments/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_withTextTooLong_shouldReturnBadRequest() throws Exception {
        String longText = "a".repeat(2001);
        NewCommentDto request = NewCommentDto.builder()
                .text(longText)
                .build();

        mvc.perform(post("/users/{userId}/comments/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_withNullText_shouldReturnBadRequest() throws Exception {
        NewCommentDto request = NewCommentDto.builder()
                .text(null)
                .build();

        mvc.perform(post("/users/{userId}/comments/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_withNegativeUserId_shouldReturnBadRequest() throws Exception {
        NewCommentDto request = NewCommentDto.builder()
                .text("Great event!")
                .build();

        mvc.perform(post("/users/{userId}/comments/events/{eventId}", -1L, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_withNegativeEventId_shouldReturnBadRequest() throws Exception {
        NewCommentDto request = NewCommentDto.builder()
                .text("Great event!")
                .build();

        mvc.perform(post("/users/{userId}/comments/events/{eventId}", userId, -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_shouldReturnUpdatedComment() throws Exception {
        UpdateCommentDto request = UpdateCommentDto.builder()
                .text("Updated comment text")
                .build();

        when(service.updateComment(eq(userId), eq(commentId), any(UpdateCommentDto.class)))
                .thenReturn(createUpdatedCommentDto());

        mvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Updated comment text"))
                .andExpect(jsonPath("$.eventId").value(eventId));
    }

    @Test
    void updateComment_withEmptyText_shouldReturnBadRequest() throws Exception {
        UpdateCommentDto request = UpdateCommentDto.builder()
                .text("")
                .build();

        mvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_withTextTooLong_shouldReturnBadRequest() throws Exception {
        String longText = "a".repeat(2001);
        UpdateCommentDto request = UpdateCommentDto.builder()
                .text(longText)
                .build();

        mvc.perform(patch("/users/{userId}/comments/{commentId}", userId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_withNegativeUserId_shouldReturnBadRequest() throws Exception {
        UpdateCommentDto request = UpdateCommentDto.builder()
                .text("Updated text")
                .build();

        mvc.perform(patch("/users/{userId}/comments/{commentId}", -1L, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_withNegativeCommentId_shouldReturnBadRequest() throws Exception {
        UpdateCommentDto request = UpdateCommentDto.builder()
                .text("Updated text")
                .build();

        mvc.perform(patch("/users/{userId}/comments/{commentId}", userId, -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_shouldReturnOk() throws Exception {
        doNothing().when(service).deleteCommentByUser(eq(userId), eq(commentId));

        mvc.perform(delete("/users/{userId}/comments/{commentId}", userId, commentId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteComment_withNegativeUserId_shouldReturnBadRequest() throws Exception {
        mvc.perform(delete("/users/{userId}/comments/{commentId}", -1L, commentId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_withNegativeCommentId_shouldReturnBadRequest() throws Exception {
        mvc.perform(delete("/users/{userId}/comments/{commentId}", userId, -1L))
                .andExpect(status().isBadRequest());
    }

    private CommentDto createCommentDto() {
        UserShortDto author = UserShortDto.builder()
                .id(userId)
                .name("User1")
                .build();

        return CommentDto.builder()
                .id(1L)
                .text("Great event!")
                .eventId(eventId)
                .author(author)
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    private CommentDto createUpdatedCommentDto() {
        UserShortDto author = UserShortDto.builder()
                .id(userId)
                .name("User1")
                .build();

        return CommentDto.builder()
                .id(1L)
                .text("Updated comment text")
                .eventId(eventId)
                .author(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }
}