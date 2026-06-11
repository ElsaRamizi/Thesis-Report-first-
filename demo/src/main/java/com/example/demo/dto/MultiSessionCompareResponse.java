package com.example.demo.dto;

import java.util.List;

public record MultiSessionCompareResponse(
    List<SessionResultResponse> sessions,
    List<SessionTimelinePointDto> timeline,
    MetricComparisonDto baselineComparison,
    String summary
) {}
