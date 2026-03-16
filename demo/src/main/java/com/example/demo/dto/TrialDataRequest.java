package com.example.demo.dto;

import java.time.LocalDateTime;

public class TrialDataRequest {

    private String stimulus;
    private String response;
    private Long reactionTime;
    private boolean correct;
    private LocalDateTime timestamp;

    public TrialDataRequest() {
    }

    public String getStimulus() {
        return stimulus;
    }

    public void setStimulus(String stimulus) {
        this.stimulus = stimulus;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Long getReactionTime() {
        return reactionTime;
    }

    public void setReactionTime(Long reactionTime) {
        this.reactionTime = reactionTime;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
