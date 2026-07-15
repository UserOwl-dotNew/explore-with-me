package ru.practicum.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {
    /**
     * Широта и долгота места проведения события
     */
    @Column(name = "lat")
    private Float lat;

    @Column(name = "lon")
    private Float lon;
}
