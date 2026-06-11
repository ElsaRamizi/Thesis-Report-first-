package com.example.demo.dto;

import java.time.LocalDate;

public record CognitiveExportRequest(
    String taskType,
    LocalDate startDate,
    LocalDate endDate,
    Boolean assignedOnly
) {}
