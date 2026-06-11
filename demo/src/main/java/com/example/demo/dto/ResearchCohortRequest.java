package com.example.demo.dto;

import java.util.List;

public record ResearchCohortRequest(
    String name,
    String description,
    List<ResearchFilterCriterion> filters
) {}
