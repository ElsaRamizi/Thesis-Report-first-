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
@Table(name = "research_questions")
public class ResearchQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_id", nullable = false)
    private ResearchStudy study;

    @Column(nullable = false, length = 2000)
    private String questionText;

    @Column(nullable = false)
    private String questionType;

    @Column(length = 4000)
    private String optionsJson;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int sortOrder;

    private String questionKey;

    @Column(nullable = false)
    private boolean demographicDefault;

    public ResearchQuestion() {}

    public Long getId() {
        return id;
    }

    public ResearchStudy getStudy() {
        return study;
    }

    public void setStudy(ResearchStudy study) {
        this.study = study;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getQuestionKey() {
        return questionKey;
    }

    public void setQuestionKey(String questionKey) {
        this.questionKey = questionKey;
    }

    public boolean isDemographicDefault() {
        return demographicDefault;
    }

    public void setDemographicDefault(boolean demographicDefault) {
        this.demographicDefault = demographicDefault;
    }
}
