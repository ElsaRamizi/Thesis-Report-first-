package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResearchParticipationResponse(
    Long id,
    Long studyId,
    String studyTitle,
    String researcherName,
    String researchType,
    boolean rewarded,
    String rewardDetails,
    boolean anonymous,
    String displayName,
    String status,
    int progressPercent,
    LocalDateTime joinedAt,
    LocalDateTime withdrawnAt,
    List<ResearchAnswerRequest> answers
) {}
