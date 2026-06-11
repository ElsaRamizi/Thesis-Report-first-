package com.example.demo.dto;

import java.time.LocalDateTime;

public record CognitiveTimelinePointDto(
    Long sessionId,
    String label,
    String taskType,
    LocalDateTime startTime,
    Double avgReactionTime,
    Double accuracy,
    Double errorRate,
    Double missRate,
    boolean rolling
) {}
