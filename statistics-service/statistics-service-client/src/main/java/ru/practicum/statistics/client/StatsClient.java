package ru.practicum.statistics.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClient {

    private final RestTemplate restTemplate;

    @Value("${stats-server.url:http://localhost:9090}")
    private String serverUrl;

    public void sendHit(EndpointHit hit) {
        try {
            restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
            log.info("Отправлен хит: {}", hit);
        } catch (Exception e) {
            log.error("Ошибка при отправке хита: {}", e.getMessage());
        }
    }

    public List<ViewStats> getStats(String start, String end, List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", start)
                    .queryParam("end", end);
            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }
            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            ViewStats[] response = restTemplate.getForObject(builder.toUriString(), ViewStats[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
