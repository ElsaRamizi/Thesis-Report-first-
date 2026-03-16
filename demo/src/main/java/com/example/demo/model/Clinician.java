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
@Table(name = "clinicians")
public class Clinician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(nullable = false)
    private String organization;

    public Clinician() {}

    public Clinician(User userId, String organization) {
        this.userId = userId;
        this.organization = organization;
    }

    public Long getId() {
        return id;
    }

    public User getUserId() {
        return userId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
