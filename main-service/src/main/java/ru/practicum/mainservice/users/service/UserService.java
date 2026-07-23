package ru.practicum.mainservice.users.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.common.dto.UserDto;
import ru.practicum.common.entity.User;
import ru.practicum.mainservice.users.dto.NewUserRequest;

import java.util.List;

public interface UserService {

    /**
     * Получение пользователей с фильтрацией по идентификаторам
     * и пагинацией.
     *
     * @param ids      идентификаторы пользователей;
     *                 если не переданы, возвращаются все пользователи
     * @param pageable параметры пагинации
     * @return найденные пользователи
     */
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    /**
     * Создание пользователя.
     *
     * @param request данные нового пользователя
     * @return созданный пользователь
     */
    UserDto createUser(NewUserRequest request);

    /**
     * Удаление пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void deleteUser(Long userId);

    /**
     * Получение сущности пользователя.
     *
     * Используется другими сервисами.
     *
     * @param userId идентификатор пользователя
     * @return сущность пользователя
     */
    User getUserEntity(Long userId);
}