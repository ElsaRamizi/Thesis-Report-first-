package com.example.demo.model;

import com.example.demo.security.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinician_annotations")
public class ClinicianAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "clinician_id", nullable = false)
    private User clinician;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private TestSession session;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 8000)
    private String content;

    private LocalDateTime createdAt;

    public ClinicianAnnotation() {}

    public Long getId() {
        return id;
    }

    public User getClinician() {
        return clinician;
    }

    public void setClinician(User clinician) {
        this.clinician = clinician;
    }

    public User getParticipant() {
        return participant;
    }

    public void setParticipant(User participant) {
        this.participant = participant;
    }

    public TestSession getSession() {
        return session;
    }

    public void setSession(TestSession session) {
        this.session = session;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
