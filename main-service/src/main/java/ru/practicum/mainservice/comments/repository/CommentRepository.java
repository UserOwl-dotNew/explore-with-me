package ru.practicum.mainservice.comments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.comments.entity.Comment;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEventIdAndDeletedFalse(Long eventId, Pageable pageable);

    Optional<Comment> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    boolean existsByIdAndAuthorIdAndDeletedFalse(Long commentId, Long authorId);

    Page<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);

    void deleteAllByAuthorId(Long userId);

    long countByEventIdAndDeletedFalse(Long eventId);
}
