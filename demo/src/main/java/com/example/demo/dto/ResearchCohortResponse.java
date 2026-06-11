package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResearchCohortResponse(
    Long id,
    String name,
    String description,
    List<ResearchFilterCriterion> filters,
    int matchedParticipantCount,
    LocalDateTime createdAt
) {}
