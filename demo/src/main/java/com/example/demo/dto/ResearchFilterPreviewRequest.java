package com.example.demo.dto;

import java.util.List;

public record ResearchFilterPreviewRequest(
    List<ResearchFilterCriterion> filters
) {}
