package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.EventRequestStatusUpdateRequest;
import ru.practicum.mainservice.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.service.ParticipationRequestService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class ParticipationRequestPrivateController {

    private final ParticipationRequestService requestService;

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable @Positive Long userId,
                                              @RequestParam @Positive Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable @Positive Long userId,
                                                              @PathVariable @Positive Long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }
}
