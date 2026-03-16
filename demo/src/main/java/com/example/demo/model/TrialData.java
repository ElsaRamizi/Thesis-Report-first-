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
@Table(name = "trial_data")
public class TrialData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession sessionId;

    @Column(nullable = false)
    private String stimulus;

    @Column(nullable = false)
    private String response;

    @Column(nullable = false)
    private Long reactionTime;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public TrialData() {}

    public TrialData(TestSession sessionId, String stimulus, String response, Long reactionTime, boolean correct, LocalDateTime timestamp) {
        this.sessionId = sessionId;
        this.stimulus = stimulus;
        this.response = response;
        this.reactionTime = reactionTime;
        this.correct = correct;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public TestSession getSessionId() {
        return sessionId;
    }

    public String getStimulus() {
        return stimulus;
    }

    public String getResponse() {
        return response;
    }

    public Long getReactionTime() {
        return reactionTime;
    }

    public boolean isCorrect() {
        return correct;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setSessionId(TestSession sessionId) {
        this.sessionId = sessionId;
    }

    public void setStimulus(String stimulus) {
        this.stimulus = stimulus;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setReactionTime(Long reactionTime) {
        this.reactionTime = reactionTime;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
