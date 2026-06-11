package com.example.demo.dto;

public record ResearchGameCorrelation(
    Double avgAccuracy,
    Double avgReactionTime,
    Double avgDPrime,
    int sessionsAnalyzed
) {}
