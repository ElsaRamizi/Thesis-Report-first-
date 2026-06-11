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
@Table(name = "research_cohorts")
public class ResearchCohort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_id", nullable = false)
    private ResearchStudy study;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 8000)
    private String filterCriteriaJson;

    private LocalDateTime createdAt;

    public ResearchCohort() {}

    public Long getId() {
        return id;
    }

    public ResearchStudy getStudy() {
        return study;
    }

    public void setStudy(ResearchStudy study) {
        this.study = study;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilterCriteriaJson() {
        return filterCriteriaJson;
    }

    public void setFilterCriteriaJson(String filterCriteriaJson) {
        this.filterCriteriaJson = filterCriteriaJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
