package com.example.demo.dto;

public record CohortStatisticsDto(
    int participantCount,
    Double avgReactionTime,
    Double medianReactionTime,
    Double avgAccuracy,
    Double avgErrorRate,
    Double avgFalseAlarmRate,
    Double avgMaxNReached,
    Double reactionTimeStdDev,
    Double accuracyVariance
) {}
