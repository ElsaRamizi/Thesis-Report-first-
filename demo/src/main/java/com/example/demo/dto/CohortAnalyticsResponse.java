package com.example.demo.dto;

import java.util.List;

public record CohortAnalyticsResponse(
    int participantCount,
    CognitiveMetricsDto averageMetrics,
    CohortStatisticsDto statistics,
    List<CognitiveTimelinePointDto> cohortTimeline,
    List<HistogramBucketDto> reactionTimeHistogram,
    List<CohortComparisonGroupDto> comparisonGroups
) {}
