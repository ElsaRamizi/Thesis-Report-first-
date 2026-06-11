package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DoctorConnectionResponse(
    Long id,
    String doctorName,
    String doctorSurname,
    String doctorEmail,
    String institution,
    String specialization,
    boolean shareFullIdentifiable,
    boolean shareAnonymizedOnly,
    boolean shareSelectedGamesOnly,
    boolean shareQuestionnaires,
    boolean shareAnalyticsOnly,
    List<String> selectedGames,
    boolean useAnonymousSharing,
    String anonymousIdentifier,
    boolean active,
    LocalDateTime consentAcceptedAt,
    LocalDateTime revokedAt,
    LocalDateTime createdAt
) {}
