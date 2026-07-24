package ru.practicum.mainservice.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.common.entity.User;

import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByIdIn(
            Collection<Long> ids,
            Pageable pageable
    );
}
