package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "aggregated_metrics")
public class AggregatedMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession sessionId;

    @Column(nullable = false)
    private Double avgReactionTime;

    @Column(nullable = false)
    private Double accuracy;

    @Column(nullable = false)
    private Double errorRate;

    private Double falseAlarmRate;

    private Integer maxNReached;

    private Double dPrime;

    public AggregatedMetrics() {}

    public AggregatedMetrics(TestSession sessionId, Double avgReactionTime, Double accuracy, Double errorRate) {
        this.sessionId = sessionId;
        this.avgReactionTime = avgReactionTime;
        this.accuracy = accuracy;
        this.errorRate = errorRate;
    }

    public AggregatedMetrics(
        TestSession sessionId,
        Double avgReactionTime,
        Double accuracy,
        Double errorRate,
        Double falseAlarmRate,
        Integer maxNReached,
        Double dPrime
    ) {
        this(sessionId, avgReactionTime, accuracy, errorRate);
        this.falseAlarmRate = falseAlarmRate;
        this.maxNReached = maxNReached;
        this.dPrime = dPrime;
    }

    public Long getId() {
        return id;
    }

    public TestSession getSessionId() {
        return sessionId;
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

    public void setSessionId(TestSession sessionId) {
        this.sessionId = sessionId;
    }

    public void setAvgReactionTime(Double avgReactionTime) {
        this.avgReactionTime = avgReactionTime;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public void setFalseAlarmRate(Double falseAlarmRate) {
        this.falseAlarmRate = falseAlarmRate;
    }

    public void setMaxNReached(Integer maxNReached) {
        this.maxNReached = maxNReached;
    }

    public void setDPrime(Double dPrime) {
        this.dPrime = dPrime;
    }
}
