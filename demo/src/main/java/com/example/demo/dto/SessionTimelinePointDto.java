package com.example.demo.dto;

import java.time.LocalDateTime;

public record SessionTimelinePointDto(
    Long sessionId,
    String taskType,
    String taskTitle,
    LocalDateTime startTime,
    Double accuracy,
    Double avgReactionTime
) {}
