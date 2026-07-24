package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.enums.RequestStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}
