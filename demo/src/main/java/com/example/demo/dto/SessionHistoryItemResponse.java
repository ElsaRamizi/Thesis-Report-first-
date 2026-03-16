package com.example.demo.dto;

import java.time.LocalDateTime;

public class SessionHistoryItemResponse {

    private Long sessionId;
    private String taskType;
    private String taskTitle;
    private String difficultyLevel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double avgReactionTime;
    private Double accuracy;
    private Double errorRate;

    public SessionHistoryItemResponse() {
    }

    public SessionHistoryItemResponse(
        Long sessionId,
        String taskType,
        String taskTitle,
        String difficultyLevel,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Double avgReactionTime,
        Double accuracy,
        Double errorRate
    ) {
        this.sessionId = sessionId;
        this.taskType = taskType;
        this.taskTitle = taskTitle;
        this.difficultyLevel = difficultyLevel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.avgReactionTime = avgReactionTime;
        this.accuracy = accuracy;
        this.errorRate = errorRate;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Double getAvgReactionTime() {
        return avgReactionTime;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public Double getErrorRate() {
        return errorRate;
    }
}
