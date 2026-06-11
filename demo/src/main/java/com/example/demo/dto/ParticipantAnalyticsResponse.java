package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ParticipantAnalyticsResponse(
    Long participantId,
    String displayName,
    boolean anonymous,
    String sharingSource,
    CognitiveMetricsDto latestMetrics,
    CognitiveMetricsDto overallAverage,
    CognitiveMetricsDto rollingAverage,
    CognitiveMetricsDto cohortAverage,
    MetricComparisonDto cohortComparison,
    CognitiveProfileDto cognitiveProfile,
    List<CognitiveTimelinePointDto> timeline,
    List<CognitiveTimelinePointDto> rollingTimeline,
    List<SessionAnalyticsDto> sessions
) {}
