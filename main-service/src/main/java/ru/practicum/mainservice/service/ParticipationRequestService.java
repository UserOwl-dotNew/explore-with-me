package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.EventRequestStatusUpdateRequest;
import ru.practicum.mainservice.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainservice.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);
    ParticipationRequestDto addRequest(Long userId, Long eventId);
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);
    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);
}
