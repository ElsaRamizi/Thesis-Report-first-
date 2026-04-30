package com.example.demo.dto;

public class TrialSubmitRequest extends TrialDataRequest {

    private Long sessionId;

    public TrialSubmitRequest() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
