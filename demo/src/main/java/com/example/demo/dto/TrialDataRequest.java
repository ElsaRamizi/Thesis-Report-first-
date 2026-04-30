package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class TrialDataRequest {

    private String stimulus;
    private String response;
    private Long reactionTime;
    private boolean correct;
    private LocalDateTime timestamp;
    private Integer trialIndex;
    @JsonProperty("nLevel")
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

    public TrialDataRequest() {
    }

    public String getStimulus() {
        return stimulus;
    }

    public void setStimulus(String stimulus) {
        this.stimulus = stimulus;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Long getReactionTime() {
        return reactionTime;
    }

    public void setReactionTime(Long reactionTime) {
        this.reactionTime = reactionTime;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getTrialIndex() {
        return trialIndex;
    }

    public void setTrialIndex(Integer trialIndex) {
        this.trialIndex = trialIndex;
    }

    public Integer getNLevel() {
        return nLevel;
    }

    public void setNLevel(Integer nLevel) {
        this.nLevel = nLevel;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public Boolean getExpectedPositionMatch() {
        return expectedPositionMatch;
    }

    public void setExpectedPositionMatch(Boolean expectedPositionMatch) {
        this.expectedPositionMatch = expectedPositionMatch;
    }

    public Boolean getExpectedLetterMatch() {
        return expectedLetterMatch;
    }

    public void setExpectedLetterMatch(Boolean expectedLetterMatch) {
        this.expectedLetterMatch = expectedLetterMatch;
    }

    public Boolean getUserPressedPosition() {
        return userPressedPosition;
    }

    public void setUserPressedPosition(Boolean userPressedPosition) {
        this.userPressedPosition = userPressedPosition;
    }

    public Boolean getUserPressedLetter() {
        return userPressedLetter;
    }

    public void setUserPressedLetter(Boolean userPressedLetter) {
        this.userPressedLetter = userPressedLetter;
    }

    public String getPositionOutcome() {
        return positionOutcome;
    }

    public void setPositionOutcome(String positionOutcome) {
        this.positionOutcome = positionOutcome;
    }

    public String getLetterOutcome() {
        return letterOutcome;
    }

    public void setLetterOutcome(String letterOutcome) {
        this.letterOutcome = letterOutcome;
    }

    public Long getReactionTimePosition() {
        return reactionTimePosition;
    }

    public void setReactionTimePosition(Long reactionTimePosition) {
        this.reactionTimePosition = reactionTimePosition;
    }

    public Long getReactionTimeLetter() {
        return reactionTimeLetter;
    }

    public void setReactionTimeLetter(Long reactionTimeLetter) {
        this.reactionTimeLetter = reactionTimeLetter;
    }

    public LocalDateTime getStimulusStartTime() {
        return stimulusStartTime;
    }

    public void setStimulusStartTime(LocalDateTime stimulusStartTime) {
        this.stimulusStartTime = stimulusStartTime;
    }
}
