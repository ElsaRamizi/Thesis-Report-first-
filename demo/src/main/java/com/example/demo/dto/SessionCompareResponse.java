package com.example.demo.dto;

public record SessionCompareResponse(
    SessionResultResponse sessionA,
    SessionResultResponse sessionB,
    MetricComparisonDto comparison
) {}
