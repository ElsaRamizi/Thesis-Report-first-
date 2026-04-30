package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrialResultResponse {

    private Long id;
    private String stimulus;
    private String response;
    private boolean correct;
    private Long reactionTime;
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

    public TrialResultResponse() {
    }

    public TrialResultResponse(Long id, String stimulus, String response, boolean correct, Long reactionTime) {
        this.id = id;
        this.stimulus = stimulus;
        this.response = response;
        this.correct = correct;
        this.reactionTime = reactionTime;
    }

    public TrialResultResponse(
        Long id,
        String stimulus,
        String response,
        boolean correct,
        Long reactionTime,
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
        Long reactionTimeLetter
    ) {
        this(id, stimulus, response, correct, reactionTime);
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
    }

    public Long getId() {
        return id;
    }

    public String getStimulus() {
        return stimulus;
    }

    public String getResponse() {
        return response;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Long getReactionTime() {
        return reactionTime;
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
}
