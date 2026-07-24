package ru.practicum.mainservice.categories.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.common.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
