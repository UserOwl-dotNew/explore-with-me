package ru.practicum.mainservice.users.service;

import ru.practicum.common.entity.User;

public interface UserService {
    User getUserEntity(Long userId);
}
