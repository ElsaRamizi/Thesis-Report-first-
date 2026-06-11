package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public record ResearchAnalyticsResponse(
    int totalParticipants,
    int anonymousParticipants,
    int namedParticipants,
    Map<String, Integer> statusBreakdown,
    List<ResearchQuestionAnalytics> questionAnalytics,
    ResearchGameCorrelation gameCorrelation,
    List<ResearchTrendPoint> enrollmentTrend,
    List<ResearchParticipantSummary> participants
) {}
