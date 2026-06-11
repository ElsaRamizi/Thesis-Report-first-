package com.example.demo.dto;

import java.time.LocalDateTime;

public record AnnotationRequest(
    String content,
    Long sessionId
) {}
