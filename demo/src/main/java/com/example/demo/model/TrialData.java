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

    private Integer trialIndex;

    private Integer nLevel;

    private Integer position;

    private String letter;

    private Boolean expectedPositionMatch;

    private Boolean expectedLetterMatch;

    private Boolean userPressedPosition;

    private Boolean userPressedLetter;

    private String positionOutcome;

    private String letterOutcome;

    private Long reactionTimePosition;

    private Long reactionTimeLetter;

    private LocalDateTime stimulusStartTime;

    public TrialData() {}

    public TrialData(TestSession sessionId, String stimulus, String response, Long reactionTime, boolean correct, LocalDateTime timestamp) {
        this.sessionId = sessionId;
        this.stimulus = stimulus;
        this.response = response;
        this.reactionTime = reactionTime;
        this.correct = correct;
        this.timestamp = timestamp;
    }

    public TrialData(
        TestSession sessionId,
        String stimulus,
        String response,
        Long reactionTime,
        boolean correct,
        LocalDateTime timestamp,
        Integer trialIndex,
        Integer nLevel,
        Integer position,
        String letter,
        Boolean expectedPositionMatch,
        Boolean expectedLetterMatch,
        Boolean userPressedPosition,
        Boolean userPressedLetter,
        String positionOutcome,
        String letterOutcome,
        Long reactionTimePosition,
        Long reactionTimeLetter,
        LocalDateTime stimulusStartTime
    ) {
        this(sessionId, stimulus, response, reactionTime, correct, timestamp);
        this.trialIndex = trialIndex;
        this.nLevel = nLevel;
        this.position = position;
        this.letter = letter;
        this.expectedPositionMatch = expectedPositionMatch;
        this.expectedLetterMatch = expectedLetterMatch;
        this.userPressedPosition = userPressedPosition;
        this.userPressedLetter = userPressedLetter;
        this.positionOutcome = positionOutcome;
        this.letterOutcome = letterOutcome;
        this.reactionTimePosition = reactionTimePosition;
        this.reactionTimeLetter = reactionTimeLetter;
        this.stimulusStartTime = stimulusStartTime;
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

    public Integer getTrialIndex() {
        return trialIndex;
    }

    public Integer getNLevel() {
        return nLevel;
    }

    public Integer getPosition() {
        return position;
    }

    public String getLetter() {
        return letter;
    }

    public Boolean getExpectedPositionMatch() {
        return expectedPositionMatch;
    }

    public Boolean getExpectedLetterMatch() {
        return expectedLetterMatch;
    }

    public Boolean getUserPressedPosition() {
        return userPressedPosition;
    }

    public Boolean getUserPressedLetter() {
        return userPressedLetter;
    }

    public String getPositionOutcome() {
        return positionOutcome;
    }

    public String getLetterOutcome() {
        return letterOutcome;
    }

    public Long getReactionTimePosition() {
        return reactionTimePosition;
    }

    public Long getReactionTimeLetter() {
        return reactionTimeLetter;
    }

    public LocalDateTime getStimulusStartTime() {
        return stimulusStartTime;
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

    public void setTrialIndex(Integer trialIndex) {
        this.trialIndex = trialIndex;
    }

    public void setNLevel(Integer nLevel) {
        this.nLevel = nLevel;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public void setExpectedPositionMatch(Boolean expectedPositionMatch) {
        this.expectedPositionMatch = expectedPositionMatch;
    }

    public void setExpectedLetterMatch(Boolean expectedLetterMatch) {
        this.expectedLetterMatch = expectedLetterMatch;
    }

    public void setUserPressedPosition(Boolean userPressedPosition) {
        this.userPressedPosition = userPressedPosition;
    }

    public void setUserPressedLetter(Boolean userPressedLetter) {
        this.userPressedLetter = userPressedLetter;
    }

    public void setPositionOutcome(String positionOutcome) {
        this.positionOutcome = positionOutcome;
    }

    public void setLetterOutcome(String letterOutcome) {
        this.letterOutcome = letterOutcome;
    }

    public void setReactionTimePosition(Long reactionTimePosition) {
        this.reactionTimePosition = reactionTimePosition;
    }

    public void setReactionTimeLetter(Long reactionTimeLetter) {
        this.reactionTimeLetter = reactionTimeLetter;
    }

    public void setStimulusStartTime(LocalDateTime stimulusStartTime) {
        this.stimulusStartTime = stimulusStartTime;
    }
}
