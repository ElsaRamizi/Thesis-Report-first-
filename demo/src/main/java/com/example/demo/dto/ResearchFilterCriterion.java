package com.example.demo.dto;

import java.util.List;

public record ResearchFilterCriterion(
    Long questionId,
    String questionKey,
    String operator,
    String value,
    Integer minAge,
    Integer maxAge,
    List<String> values
) {}
