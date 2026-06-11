package com.example.demo.dto;

import java.util.List;

public record SessionAnalysisDto(
    int totalTrials,
    long incorrectResponses,
    List<DistributionBucketDto> reactionTimeDistribution,
    Double fatigueIndicatorPercent,
    List<String> anomalies
) {}
