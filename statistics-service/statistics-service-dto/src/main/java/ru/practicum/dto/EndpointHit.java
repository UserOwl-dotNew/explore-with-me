package ru.practicum.dto;

import java.time.LocalDateTime;

public class EndpointHit {
    public Long id;
    public String app;
    public String uri;
    public String ip;
    public LocalDateTime timestamp;
}