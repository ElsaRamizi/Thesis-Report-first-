package com.example.demo.dto;

import java.time.LocalDateTime;

public record GroupTimelinePointDto(
    String label,
    LocalDateTime bucketStart,
    Double avgAccuracy,
    Double avgReactionTime,
    int sessionCount
) {}
