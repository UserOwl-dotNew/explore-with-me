package ru.practicum.statistics.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.statistics.server.model.HitEntity;
import ru.practicum.statistics.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService service;

    @GetMapping("/stats")
    public Collection<ViewStats> getAll(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                        @RequestParam(required = false) List<String> uris,
                                        @RequestParam(defaultValue = "false") boolean unique) {
        log.info("GET /stats REQUEST: start= {} end= {} uris_count= {} unique= {}",
                start, end, uris != null ? uris.size() : 0, unique);
        List<ViewStats> stats = service.getStats(start, end, uris, unique);
        log.info("GET /stats RESPONSE: stats_size= {}", stats.size());
        return stats;
    }

    @PostMapping("/hit")
    public void post(@RequestBody EndpointHit endpointHit) {
        log.info("POST /hit REQUEST: endpointHit= {}", endpointHit);
        HitEntity hit = service.save(endpointHit);
        log.info("POST / hit RESPONSE: hit= {}", hit);
    }
}
