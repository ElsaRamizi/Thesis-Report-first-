package com.example.demo.dto;

import java.util.List;

public record ResearchCohortComparison(
    String label,
    int participantCount,
    List<ResearchQuestionAnalytics> questionAnalytics,
    ResearchGameCorrelation gameCorrelation
) {}
