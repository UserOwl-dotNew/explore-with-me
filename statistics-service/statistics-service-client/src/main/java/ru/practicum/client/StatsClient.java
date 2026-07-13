package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import org.springframework.http.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(RestTemplate restTemplate,
                       @Value("${stats-server.url:http://localhost:9090}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public void hit(EndpointHit hit) {
        try {
            String url = serverUrl + "/hit";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity(url, requestEntity, Void.class);

            if (response.getStatusCode() != HttpStatus.CREATED) {
                log.warn("Stats server returned unexpected status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send hit to stats server: {}", e.getMessage());
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        try {
            String encodedStart = URLEncoder.encode(start.toString(), StandardCharsets.UTF_8);
            String encodedEnd = URLEncoder.encode(end.toString(), StandardCharsets.UTF_8);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", encodedStart)
                    .queryParam("end", encodedEnd)
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", uris.toArray(new String[0]));
            }

            String url = builder.build().toUriString();

            HttpEntity<List<ViewStats>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStats>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch stats: {}", e.getMessage());
            return List.of();
        }
    }
}
