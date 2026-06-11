package com.example.demo.dto;

import java.util.List;

public record ResearchQuestionRequest(
    String questionText,
    String questionType,
    List<String> options,
    boolean required,
    int sortOrder
) {}
