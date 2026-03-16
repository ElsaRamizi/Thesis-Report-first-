package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SessionResultResponse {

    private Long sessionId;
    private String taskType;
    private String taskTitle;
    private String difficultyLevel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double avgReactionTime;
    private Double accuracy;
    private Double errorRate;
    private List<TrialResultResponse> trials;

    public SessionResultResponse() {
    }

    public SessionResultResponse(
        Long sessionId,
        String taskType,
        String taskTitle,
        String difficultyLevel,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Double avgReactionTime,
        Double accuracy,
        Double errorRate,
        List<TrialResultResponse> trials
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
        this.trials = trials;
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

    public List<TrialResultResponse> getTrials() {
        return trials;
    }
}
