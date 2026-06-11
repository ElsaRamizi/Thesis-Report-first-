package com.example.demo.dto;

public record CognitiveProfileDto(
    Double memory,
    Double reactionSpeed,
    Double attention,
    Double consistency,
    Double inhibitionControl,
    Double adaptability
) {}
