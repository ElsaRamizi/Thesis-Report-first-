package com.example.demo.dto;

import java.time.LocalDateTime;

public record ResearchParticipantSummary(
    Long participationId,
    String displayName,
    boolean anonymous,
    String status,
    int progressPercent,
    LocalDateTime joinedAt
) {}
