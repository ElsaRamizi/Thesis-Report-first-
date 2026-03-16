package com.example.demo.dto;

public class TrialResultResponse {

    private Long id;
    private String stimulus;
    private String response;
    private boolean correct;
    private Long reactionTime;

    public TrialResultResponse() {
    }

    public TrialResultResponse(Long id, String stimulus, String response, boolean correct, Long reactionTime) {
        this.id = id;
        this.stimulus = stimulus;
        this.response = response;
        this.correct = correct;
        this.reactionTime = reactionTime;
    }

    public Long getId() {
        return id;
    }

    public String getStimulus() {
        return stimulus;
    }

    public String getResponse() {
        return response;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Long getReactionTime() {
        return reactionTime;
    }
}
