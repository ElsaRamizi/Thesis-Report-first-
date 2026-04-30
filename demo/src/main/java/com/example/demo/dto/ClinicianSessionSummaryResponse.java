package com.example.demo.dto;

import java.time.LocalDateTime;

public class ClinicianSessionSummaryResponse {

    private Long sessionId;
    private Long participantId;
    private String participantEmail;
    private String taskType;
    private String taskTitle;
    private String difficultyLevel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double avgReactionTime;
    private Double accuracy;
    private Double errorRate;
    private Double falseAlarmRate;
    private Integer maxNReached;

    public ClinicianSessionSummaryResponse() {
    }

    public ClinicianSessionSummaryResponse(
        Long sessionId,
        Long participantId,
        String participantEmail,
        String taskType,
        String taskTitle,
        String difficultyLevel,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Double avgReactionTime,
        Double accuracy,
        Double errorRate,
        Double falseAlarmRate,
        Integer maxNReached
    ) {
        this.sessionId = sessionId;
        this.participantId = participantId;
        this.participantEmail = participantEmail;
        this.taskType = taskType;
        this.taskTitle = taskTitle;
        this.difficultyLevel = difficultyLevel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.avgReactionTime = avgReactionTime;
        this.accuracy = accuracy;
        this.errorRate = errorRate;
        this.falseAlarmRate = falseAlarmRate;
        this.maxNReached = maxNReached;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public String getParticipantEmail() {
        return participantEmail;
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

    public Double getFalseAlarmRate() {
        return falseAlarmRate;
    }

    public Integer getMaxNReached() {
        return maxNReached;
    }
}
