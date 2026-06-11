package com.example.demo.dto;

import java.util.List;

public record ResearchCompareResponse(
    List<ResearchCohortComparison> groups
) {}
