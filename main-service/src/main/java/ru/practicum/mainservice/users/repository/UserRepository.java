package ru.practicum.mainservice.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.common.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
