package com.example.demo.dto;

import java.time.LocalDateTime;

public class SessionStartRequest {

    private String taskType;
    private String difficultyLevel;
    private LocalDateTime startTime;
    private Integer initialN;

    public SessionStartRequest() {
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

    public Integer getInitialN() {
        return initialN;
    }

    public void setInitialN(Integer initialN) {
        this.initialN = initialN;
    }
}
