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
@Table(name = "research_participations")
public class ResearchParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_id", nullable = false)
    private ResearchStudy study;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @Column(nullable = false)
    private boolean anonymous;

    @Column(length = 64)
    private String anonymousIdentifier;

    @Column(nullable = false)
    private boolean consentAccepted;

    @Column(nullable = false)
    private boolean dataSharingAccepted;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int progressPercent;

    private LocalDateTime joinedAt;

    private LocalDateTime withdrawnAt;

    private LocalDateTime completedAt;

    public ResearchParticipation() {}

    public Long getId() {
        return id;
    }

    public ResearchStudy getStudy() {
        return study;
    }

    public void setStudy(ResearchStudy study) {
        this.study = study;
    }

    public User getParticipant() {
        return participant;
    }

    public void setParticipant(User participant) {
        this.participant = participant;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getAnonymousIdentifier() {
        return anonymousIdentifier;
    }

    public void setAnonymousIdentifier(String anonymousIdentifier) {
        this.anonymousIdentifier = anonymousIdentifier;
    }

    public boolean isConsentAccepted() {
        return consentAccepted;
    }

    public void setConsentAccepted(boolean consentAccepted) {
        this.consentAccepted = consentAccepted;
    }

    public boolean isDataSharingAccepted() {
        return dataSharingAccepted;
    }

    public void setDataSharingAccepted(boolean dataSharingAccepted) {
        this.dataSharingAccepted = dataSharingAccepted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getWithdrawnAt() {
        return withdrawnAt;
    }

    public void setWithdrawnAt(LocalDateTime withdrawnAt) {
        this.withdrawnAt = withdrawnAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
