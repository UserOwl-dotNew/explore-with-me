package ru.practicum.mainservice.categories.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.entity.Category;
import ru.practicum.mainservice.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    // TODO: JavaDocs
    // Admin
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long catId, CategoryDto dto);

    void deleteCategory(Long catId);

    // Public
    List<CategoryDto> getCategories(Pageable pageable);

    CategoryDto getCategory(Long catId);

    // Internal (для других сервисов)
    Category getCategoryEntity(Long categoryId);
}
