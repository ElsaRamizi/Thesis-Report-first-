package com.example.demo.dto;

import java.time.LocalDateTime;

public record AnnotationResponse(
    Long id,
    Long participantId,
    Long sessionId,
    String clinicianEmail,
    String content,
    LocalDateTime createdAt
) {}
