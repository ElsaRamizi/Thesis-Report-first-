package com.example.demo.dto;

import java.time.LocalDateTime;

public class SessionEndRequest {

    private Long sessionId;
    private LocalDateTime endTime;
    private Integer finalN;

    public SessionEndRequest() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getFinalN() {
        return finalN;
    }

    public void setFinalN(Integer finalN) {
        this.finalN = finalN;
    }
}
