package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SessionCompleteRequest {

    private String taskType;
    private String difficultyLevel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<TrialDataRequest> trials;

    public SessionCompleteRequest() {
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<TrialDataRequest> getTrials() {
        return trials;
    }

    public void setTrials(List<TrialDataRequest> trials) {
        this.trials = trials;
    }
}
