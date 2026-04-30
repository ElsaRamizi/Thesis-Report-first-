package com.example.demo.dto;

public class SessionStartResponse {

    private Long sessionId;
    private String taskType;
    private Integer initialN;

    public SessionStartResponse() {
    }

    public SessionStartResponse(Long sessionId, String taskType, Integer initialN) {
        this.sessionId = sessionId;
        this.taskType = taskType;
        this.initialN = initialN;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getTaskType() {
        return taskType;
    }

    public Integer getInitialN() {
        return initialN;
    }
}
