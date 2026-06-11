package com.example.demo.dto;

public record MetricComparisonDto(
    Double reactionTimeDeltaPercent,
    Double accuracyDeltaPercent,
    Double errorRateDeltaPercent,
    Double missRateDeltaPercent,
    String summary
) {}
