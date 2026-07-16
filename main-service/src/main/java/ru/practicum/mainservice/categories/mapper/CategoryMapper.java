package ru.practicum.mainservice.categories.mapper;

import org.mapstruct.Mapper;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.entity.Category;
import ru.practicum.mainservice.categories.dto.NewCategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);

    Category toEntity(NewCategoryDto dto);

    Category toEntity(CategoryDto dto);
}
