package com.example.demo.dto;

import java.util.List;

public record ResearchStudyRequest(
    String title,
    String description,
    String instructions,
    String participationRequirements,
    String estimatedDuration,
    String researchType,
    boolean rewarded,
    String rewardDetails,
    boolean anonymousFriendly,
    String consentText,
    List<ResearchQuestionRequest> customQuestions
) {}
