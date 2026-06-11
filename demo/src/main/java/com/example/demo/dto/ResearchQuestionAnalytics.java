package com.example.demo.dto;

import java.util.Map;

public record ResearchQuestionAnalytics(
    Long questionId,
    String questionText,
    String questionType,
    Map<String, Integer> answerDistribution
) {}
