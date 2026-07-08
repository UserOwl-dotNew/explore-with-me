package ru.practicum.statistics.server.service;

import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.statistics.server.model.HitEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    HitEntity save(EndpointHit endpointHit);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
