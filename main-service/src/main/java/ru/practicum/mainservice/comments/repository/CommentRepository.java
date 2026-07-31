package ru.practicum.mainservice.comments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mainservice.comments.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
