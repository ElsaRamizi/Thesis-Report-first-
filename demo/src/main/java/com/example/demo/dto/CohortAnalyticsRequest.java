package com.example.demo.dto;

import java.util.List;

public record CohortAnalyticsRequest(
    List<Long> participantIds,
    List<Long> cohortIds,
    List<List<ResearchFilterCriterion>> filterGroups,
    Long studyId
) {}
