package com.example.demo.controller;

import com.example.demo.dto.CohortAnalyticsRequest;
import com.example.demo.dto.CohortAnalyticsResponse;
import com.example.demo.dto.ParticipantAnalyticsResponse;
import com.example.demo.dto.SharedPatientSummary;
import com.example.demo.service.CognitiveAnalyticsService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinician/analytics")
public class ClinicianAnalyticsController {

    private final CognitiveAnalyticsService cognitiveAnalyticsService;

    public ClinicianAnalyticsController(CognitiveAnalyticsService cognitiveAnalyticsService) {
        this.cognitiveAnalyticsService = cognitiveAnalyticsService;
    }

    @GetMapping("/shared-patients")
    public List<SharedPatientSummary> getSharedPatients(Authentication authentication) {
        return cognitiveAnalyticsService.getSharedPatients(authentication.getName());
    }

    @GetMapping("/participants/{participantId}")
    public ParticipantAnalyticsResponse getParticipantAnalytics(
        @PathVariable Long participantId,
        Authentication authentication
    ) {
        return cognitiveAnalyticsService.getParticipantAnalytics(authentication.getName(), participantId);
    }

    @PostMapping("/cohorts")
    public CohortAnalyticsResponse getCohortAnalytics(
        Authentication authentication,
        @RequestBody CohortAnalyticsRequest request
    ) {
        return cognitiveAnalyticsService.getCohortAnalytics(authentication.getName(), request);
    }
}
