package com.example.demo.dto;

public record CohortComparisonGroupDto(
    String label,
    int participantCount,
    CognitiveMetricsDto metrics
) {}
