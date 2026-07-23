package ru.practicum.mainservice.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.common.config.JacksonConfig;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.LocationDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.common.enums.EventState;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class EventFullDto {
    private Long id;
    private String annotation;
    private String description;
    private String title;

    private CategoryDto category;
    private UserShortDto initiator;
    private LocationDto location;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    private Long confirmedRequests;
    private Long views;

    @JsonFormat(pattern = JacksonConfig.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @JsonFormat(pattern = JacksonConfig.DATE_TIME_FORMAT)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = JacksonConfig.DATE_TIME_FORMAT)
    private LocalDateTime publishedOn;

    private EventState state;
}