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
@Table(name = "research_studies")
public class ResearchStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(length = 4000)
    private String instructions;

    @Column(length = 2000)
    private String participationRequirements;

    private String estimatedDuration;

    @Column(nullable = false)
    private String researchType;

    @Column(nullable = false)
    private boolean rewarded;

    @Column(length = 500)
    private String rewardDetails;

    @Column(nullable = false)
    private boolean anonymousFriendly;

    @Column(length = 4000)
    private String consentText;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ResearchStudy() {}

    public Long getId() {
        return id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getParticipationRequirements() {
        return participationRequirements;
    }

    public void setParticipationRequirements(String participationRequirements) {
        this.participationRequirements = participationRequirements;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getResearchType() {
        return researchType;
    }

    public void setResearchType(String researchType) {
        this.researchType = researchType;
    }

    public boolean isRewarded() {
        return rewarded;
    }

    public void setRewarded(boolean rewarded) {
        this.rewarded = rewarded;
    }

    public String getRewardDetails() {
        return rewardDetails;
    }

    public void setRewardDetails(String rewardDetails) {
        this.rewardDetails = rewardDetails;
    }

    public boolean isAnonymousFriendly() {
        return anonymousFriendly;
    }

    public void setAnonymousFriendly(boolean anonymousFriendly) {
        this.anonymousFriendly = anonymousFriendly;
    }

    public String getConsentText() {
        return consentText;
    }

    public void setConsentText(String consentText) {
        this.consentText = consentText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
