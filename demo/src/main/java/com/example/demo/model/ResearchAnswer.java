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
@Table(name = "research_answers")
public class ResearchAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participation_id", nullable = false)
    private ResearchParticipation participation;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private ResearchQuestion question;

    @Column(length = 4000)
    private String answerValue;

    public ResearchAnswer() {}

    public Long getId() {
        return id;
    }

    public ResearchParticipation getParticipation() {
        return participation;
    }

    public void setParticipation(ResearchParticipation participation) {
        this.participation = participation;
    }

    public ResearchQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ResearchQuestion question) {
        this.question = question;
    }

    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(String answerValue) {
        this.answerValue = answerValue;
    }
}
