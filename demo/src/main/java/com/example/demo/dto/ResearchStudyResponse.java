package com.example.demo.dto;

import java.time.LocalDateTime;

public record ResearchStudyResponse(
    Long id,
    String title,
    String description,
    String researcherName,
    String researchType,
    boolean rewarded,
    String rewardDetails,
    String participationRequirements,
    String estimatedDuration,
    boolean anonymousFriendly,
    String status,
    int participantCount,
    LocalDateTime createdAt
) {}
