package ru.practicum.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String timestamp;
}
