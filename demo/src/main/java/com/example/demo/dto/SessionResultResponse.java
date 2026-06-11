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
    private Double falseAlarmRate;
    private Integer maxNReached;
    private Double dPrime;
    private Double medianReactionTime;
    private Double missRate;
    private Double responseVariability;
    private Integer maxSpanReached;
    private Double stroopInterferenceMs;
    private Double stroopCongruentAccuracy;
    private Double stroopIncongruentAccuracy;
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
        Double falseAlarmRate,
        Integer maxNReached,
        Double dPrime,
        List<TrialResultResponse> trials
    ) {
        this(sessionId, taskType, taskTitle, difficultyLevel, startTime, endTime, avgReactionTime, accuracy, errorRate, trials);
        this.falseAlarmRate = falseAlarmRate;
        this.maxNReached = maxNReached;
        this.dPrime = dPrime;
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

    public Double getFalseAlarmRate() {
        return falseAlarmRate;
    }

    public Integer getMaxNReached() {
        return maxNReached;
    }

    public Double getDPrime() {
        return dPrime;
    }

    public Double getMedianReactionTime() {
        return medianReactionTime;
    }

    public void setMedianReactionTime(Double medianReactionTime) {
        this.medianReactionTime = medianReactionTime;
    }

    public Double getMissRate() {
        return missRate;
    }

    public void setMissRate(Double missRate) {
        this.missRate = missRate;
    }

    public Double getResponseVariability() {
        return responseVariability;
    }

    public void setResponseVariability(Double responseVariability) {
        this.responseVariability = responseVariability;
    }

    public Integer getMaxSpanReached() {
        return maxSpanReached;
    }

    public void setMaxSpanReached(Integer maxSpanReached) {
        this.maxSpanReached = maxSpanReached;
    }

    public Double getStroopInterferenceMs() {
        return stroopInterferenceMs;
    }

    public void setStroopInterferenceMs(Double stroopInterferenceMs) {
        this.stroopInterferenceMs = stroopInterferenceMs;
    }

    public Double getStroopCongruentAccuracy() {
        return stroopCongruentAccuracy;
    }

    public void setStroopCongruentAccuracy(Double stroopCongruentAccuracy) {
        this.stroopCongruentAccuracy = stroopCongruentAccuracy;
    }

    public Double getStroopIncongruentAccuracy() {
        return stroopIncongruentAccuracy;
    }

    public void setStroopIncongruentAccuracy(Double stroopIncongruentAccuracy) {
        this.stroopIncongruentAccuracy = stroopIncongruentAccuracy;
    }

    public List<TrialResultResponse> getTrials() {
        return trials;
    }
}
