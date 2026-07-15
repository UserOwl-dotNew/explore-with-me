package ru.practicum.common.exception;

import java.util.List;

public class ApiError {
    /**
     * Сведения об ошибке
     */
    private List<String> errors;
    private String message;
    private String reason;
    private String status;

    /**
     * Дата в формате yyyy-MM-dd HH:mm:ss
     */
    private String timestam;
}
