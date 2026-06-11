package com.example.demo.dto;

import java.util.List;

public record ResearchCompareRequest(
    List<Long> cohortIds,
    List<List<ResearchFilterCriterion>> filterGroups
) {}
