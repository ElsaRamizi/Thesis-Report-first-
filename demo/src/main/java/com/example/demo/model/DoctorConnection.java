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
@Table(name = "doctor_connections")
public class DoctorConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String doctorSurname;

    @Column(nullable = false)
    private String doctorEmail;

    private String institution;

    private String specialization;

    @Column(nullable = false)
    private boolean shareFullIdentifiable;

    @Column(nullable = false)
    private boolean shareAnonymizedOnly;

    @Column(nullable = false)
    private boolean shareSelectedGamesOnly;

    @Column(nullable = false)
    private boolean shareQuestionnaires;

    @Column(nullable = false)
    private boolean shareAnalyticsOnly;

    @Column(length = 1000)
    private String selectedGamesJson;

    @Column(nullable = false)
    private boolean useAnonymousSharing;

    @Column(length = 64)
    private String anonymousIdentifier;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime consentAcceptedAt;

    private LocalDateTime revokedAt;

    private LocalDateTime createdAt;

    public DoctorConnection() {}

    public Long getId() {
        return id;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSurname() {
        return doctorSurname;
    }

    public void setDoctorSurname(String doctorSurname) {
        this.doctorSurname = doctorSurname;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isShareFullIdentifiable() {
        return shareFullIdentifiable;
    }

    public void setShareFullIdentifiable(boolean shareFullIdentifiable) {
        this.shareFullIdentifiable = shareFullIdentifiable;
    }

    public boolean isShareAnonymizedOnly() {
        return shareAnonymizedOnly;
    }

    public void setShareAnonymizedOnly(boolean shareAnonymizedOnly) {
        this.shareAnonymizedOnly = shareAnonymizedOnly;
    }

    public boolean isShareSelectedGamesOnly() {
        return shareSelectedGamesOnly;
    }

    public void setShareSelectedGamesOnly(boolean shareSelectedGamesOnly) {
        this.shareSelectedGamesOnly = shareSelectedGamesOnly;
    }

    public boolean isShareQuestionnaires() {
        return shareQuestionnaires;
    }

    public void setShareQuestionnaires(boolean shareQuestionnaires) {
        this.shareQuestionnaires = shareQuestionnaires;
    }

    public boolean isShareAnalyticsOnly() {
        return shareAnalyticsOnly;
    }

    public void setShareAnalyticsOnly(boolean shareAnalyticsOnly) {
        this.shareAnalyticsOnly = shareAnalyticsOnly;
    }

    public String getSelectedGamesJson() {
        return selectedGamesJson;
    }

    public void setSelectedGamesJson(String selectedGamesJson) {
        this.selectedGamesJson = selectedGamesJson;
    }

    public boolean isUseAnonymousSharing() {
        return useAnonymousSharing;
    }

    public void setUseAnonymousSharing(boolean useAnonymousSharing) {
        this.useAnonymousSharing = useAnonymousSharing;
    }

    public String getAnonymousIdentifier() {
        return anonymousIdentifier;
    }

    public void setAnonymousIdentifier(String anonymousIdentifier) {
        this.anonymousIdentifier = anonymousIdentifier;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getConsentAcceptedAt() {
        return consentAcceptedAt;
    }

    public void setConsentAcceptedAt(LocalDateTime consentAcceptedAt) {
        this.consentAcceptedAt = consentAcceptedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
