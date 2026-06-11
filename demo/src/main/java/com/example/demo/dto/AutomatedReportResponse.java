package com.example.demo.dto;

import java.util.List;

public record AutomatedReportResponse(
    Long participantId,
    String displayName,
    int sessionsAnalyzed,
    List<String> findings,
    List<String> recommendations,
    String overallTrend,
    String severity
) {}
