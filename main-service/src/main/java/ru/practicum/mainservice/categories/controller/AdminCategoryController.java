package ru.practicum.mainservice.categories.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.mainservice.categories.dto.NewCategoryDto;
import ru.practicum.mainservice.categories.service.CategoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * Добавление новой категории
     * POST /admin/categories
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto dto) {
        log.info("POST /admin/categories with request: {}", dto);
        return categoryService.createCategory(dto);
    }

    /**
     * Изменение категории
     * PATCH /admin/categories/{catId}
     */
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto dto) {
        log.info("PATCH /admin/categories/{} with request: {}", catId, dto);
        return categoryService.updateCategory(catId, dto);
    }

    /**
     * Удаление категории
     * DELETE /admin/categories/{catId}
     */
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
    }
}
