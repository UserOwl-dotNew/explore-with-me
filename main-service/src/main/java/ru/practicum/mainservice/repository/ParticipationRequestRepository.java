package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.common.enums.RequestStatus;
import ru.practicum.mainservice.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByRequesterId(Long userId);
    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long userId);
    List<ParticipationRequest> findByEventId(Long eventId);
    long countByEventIdAndStatus(Long eventId, RequestStatus status);
    List<ParticipationRequest> findByEventIdAndIdIn(Long eventId, List<Long> ids);
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);
}
