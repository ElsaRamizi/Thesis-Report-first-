package com.example.demo.dto;

import java.util.List;

public class TaskDefinitionResponse {

    private String id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String difficulty;
    private List<String> metricFocus;

    public TaskDefinitionResponse() {
    }

    public TaskDefinitionResponse(
        String id,
        String title,
        String description,
        Integer durationMinutes,
        String difficulty,
        List<String> metricFocus
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.metricFocus = metricFocus;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<String> getMetricFocus() {
        return metricFocus;
    }
}
