package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResearchStudyDetailResponse(
    Long id,
    String title,
    String description,
    String instructions,
    String researcherName,
    String researchType,
    boolean rewarded,
    String rewardDetails,
    String participationRequirements,
    String estimatedDuration,
    boolean anonymousFriendly,
    String consentText,
    String status,
    int participantCount,
    LocalDateTime createdAt,
    List<ResearchQuestionResponse> questions
) {}
