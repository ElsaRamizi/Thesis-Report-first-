package com.example.demo.dto;

import java.util.List;

public record ResearchJoinRequest(
    boolean anonymous,
    boolean consentAccepted,
    boolean dataSharingAccepted,
    List<ResearchAnswerRequest> answers
) {}
