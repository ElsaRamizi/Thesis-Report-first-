package com.example.demo.dto;

import java.util.List;

public class TaskTrialResponse {

    private String prompt;
    private String stimulus;
    private String displayColor;
    private List<String> options;
    private String correctResponse;

    public TaskTrialResponse() {
    }

    public TaskTrialResponse(
        String prompt,
        String stimulus,
        String displayColor,
        List<String> options,
        String correctResponse
    ) {
        this.prompt = prompt;
        this.stimulus = stimulus;
        this.displayColor = displayColor;
        this.options = options;
        this.correctResponse = correctResponse;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getStimulus() {
        return stimulus;
    }

    public String getDisplayColor() {
        return displayColor;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectResponse() {
        return correctResponse;
    }
}
