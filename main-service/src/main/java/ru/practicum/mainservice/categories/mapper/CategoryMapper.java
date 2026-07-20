package ru.practicum.mainservice.categories.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.entity.Category;
import ru.practicum.mainservice.categories.dto.NewCategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CategoryDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    Category toEntity(NewCategoryDto dto);
}
