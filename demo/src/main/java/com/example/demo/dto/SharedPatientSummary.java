package com.example.demo.dto;

import java.time.LocalDateTime;

public record SharedPatientSummary(
    Long participantId,
    String displayName,
    boolean anonymous,
    String source,
    LocalDateTime sharedSince,
    Long studyId,
    String studyTitle
) {}
