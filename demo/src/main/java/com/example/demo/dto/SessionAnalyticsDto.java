package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SessionAnalyticsDto(
    Long sessionId,
    String taskType,
    String taskTitle,
    LocalDateTime startTime,
    CognitiveMetricsDto metrics,
    SessionAnalysisDto analysis
) {}
