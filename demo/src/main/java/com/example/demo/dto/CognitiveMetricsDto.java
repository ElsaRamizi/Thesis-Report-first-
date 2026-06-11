package com.example.demo.dto;

import java.util.List;

public record CognitiveMetricsDto(
    Double avgReactionTime,
    Double medianReactionTime,
    Double accuracy,
    Double errorRate,
    Double falseAlarmRate,
    Double missRate,
    Double maxNReached,
    Double improvementRate,
    Double responseVariability
) {}
