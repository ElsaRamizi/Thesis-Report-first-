package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_sessions")
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(nullable = false)
    private String taskType;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private String difficultyLevel;

    private Integer initialN;

    private Integer finalN;

    public TestSession() {}

    public TestSession(User userId, String taskType, LocalDateTime startTime, LocalDateTime endTime, String difficultyLevel) {
        this.userId = userId;
        this.taskType = taskType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.difficultyLevel = difficultyLevel;
    }

    public TestSession(
        User userId,
        String taskType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String difficultyLevel,
        Integer initialN,
        Integer finalN
    ) {
        this(userId, taskType, startTime, endTime, difficultyLevel);
        this.initialN = initialN;
        this.finalN = finalN;
    }

    public Long getId() {
        return id;
    }

    public User getUserId() {
        return userId;
    }

    public String getTaskType() {
        return taskType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public Integer getInitialN() {
        return initialN;
    }

    public Integer getFinalN() {
        return finalN;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public void setInitialN(Integer initialN) {
        this.initialN = initialN;
    }

    public void setFinalN(Integer finalN) {
        this.finalN = finalN;
    }
}
