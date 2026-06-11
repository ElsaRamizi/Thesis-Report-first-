package com.example.demo.dto;

import java.util.List;

public record DoctorConnectionRequest(
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
    boolean consentAccepted
) {}
