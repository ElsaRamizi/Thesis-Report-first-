package com.example.demo.dto;

import java.time.LocalDateTime;

public class ClinicianParticipantResponse {

    private Long participantId;
    private String email;
    private Integer sessionCount;
    private LocalDateTime latestSessionTime;
    private Double latestAccuracy;
    private Double latestAvgReactionTime;

    public ClinicianParticipantResponse() {
    }

    public ClinicianParticipantResponse(
        Long participantId,
        String email,
        Integer sessionCount,
        LocalDateTime latestSessionTime,
        Double latestAccuracy,
        Double latestAvgReactionTime
    ) {
        this.participantId = participantId;
        this.email = email;
        this.sessionCount = sessionCount;
        this.latestSessionTime = latestSessionTime;
        this.latestAccuracy = latestAccuracy;
        this.latestAvgReactionTime = latestAvgReactionTime;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public String getEmail() {
        return email;
    }

    public Integer getSessionCount() {
        return sessionCount;
    }

    public LocalDateTime getLatestSessionTime() {
        return latestSessionTime;
    }

    public Double getLatestAccuracy() {
        return latestAccuracy;
    }

    public Double getLatestAvgReactionTime() {
        return latestAvgReactionTime;
    }
}
