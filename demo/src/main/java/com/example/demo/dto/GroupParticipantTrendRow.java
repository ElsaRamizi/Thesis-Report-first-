package com.example.demo.dto;

import java.time.LocalDateTime;

public record GroupParticipantTrendRow(
    Long participantId,
    String displayName,
    String lastTaskType,
    String lastTaskTitle,
    LocalDateTime lastSessionTime,
    Double latestAccuracy,
    Double latestReactionTime,
    String trend,
    String severity
) {}
