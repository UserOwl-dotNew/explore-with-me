package ru.practicum.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndpointHit {
    public String app;
    public String uri;
    public String ip;
    public LocalDateTime timestamp;
}