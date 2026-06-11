package com.example.demo.controller;

import com.example.demo.dto.AnnotationRequest;
import com.example.demo.dto.AnnotationResponse;
import com.example.demo.dto.AutomatedReportResponse;
import com.example.demo.dto.ClinicianParticipantResponse;
import com.example.demo.dto.ClinicianSessionSummaryResponse;
import com.example.demo.dto.CognitiveExportRequest;
import com.example.demo.dto.GroupTrendsResponse;
import com.example.demo.dto.MultiSessionCompareRequest;
import com.example.demo.dto.MultiSessionCompareResponse;
import com.example.demo.dto.ParticipantProfileResponse;
import com.example.demo.dto.ParticipantProfileUpdateRequest;
import com.example.demo.dto.SessionCompareResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.service.ClinicianAnnotationService;
import com.example.demo.service.ClinicianService;
import com.example.demo.service.ParticipantProfileService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinician")
// clinician-only API — SecurityConfig requires ROLE_CLINICIAN
public class ClinicianController {

    private final ClinicianService clinicianService;
    private final ParticipantProfileService participantProfileService;
    private final ClinicianAnnotationService annotationService;

    public ClinicianController(
        ClinicianService clinicianService,
        ParticipantProfileService participantProfileService,
        ClinicianAnnotationService annotationService
    ) {
        this.clinicianService = clinicianService;
        this.participantProfileService = participantProfileService;
        this.annotationService = annotationService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Protected clinician dashboard endpoint";
    }

    /// list participants this clinician can access
    @GetMapping("/participants")
    public List<ClinicianParticipantResponse> getParticipants(Authentication authentication) {
        return clinicianService.getParticipants(authentication.getName());
    }

    @GetMapping("/directory")
    public List<ClinicianParticipantResponse> getClinicianDirectory() {
        return clinicianService.getClinicianDirectory();
    }

    @GetMapping("/group-trends")
    public GroupTrendsResponse getGroupTrends(
        Authentication authentication,
        @RequestParam(required = false, defaultValue = "all") String taskType
    ) {
        return clinicianService.getGroupTrends(authentication.getName(), taskType);
    }

    @GetMapping("/participants/{participantId}/sessions")
    public List<ClinicianSessionSummaryResponse> getParticipantSessions(
        @PathVariable Long participantId,
        Authentication authentication
    ) {
        return clinicianService.getParticipantSessions(authentication.getName(), participantId);
    }

    @GetMapping("/participants/{participantId}/profile")
    public ParticipantProfileResponse getParticipantProfile(
        @PathVariable Long participantId,
        Authentication authentication
    ) {
        return participantProfileService.getParticipantProfile(authentication.getName(), participantId);
    }

    @PutMapping("/participants/{participantId}/profile")
    public ParticipantProfileResponse updateParticipantProfile(
        @PathVariable Long participantId,
        Authentication authentication,
        @RequestBody ParticipantProfileUpdateRequest request
    ) {
        return participantProfileService.updateParticipantProfile(
            authentication.getName(),
            participantId,
            request
        );
    }

    @GetMapping("/participants/{participantId}/annotations")
    public List<AnnotationResponse> getParticipantAnnotations(
        @PathVariable Long participantId,
        Authentication authentication
    ) {
        return annotationService.getParticipantAnnotations(authentication.getName(), participantId);
    }

    @PostMapping("/participants/{participantId}/annotations")
    public AnnotationResponse addParticipantAnnotation(
        @PathVariable Long participantId,
        Authentication authentication,
        @RequestBody AnnotationRequest request
    ) {
        return annotationService.addAnnotation(authentication.getName(), participantId, request);
    }

    /// rule-based text report on accuracy/RT trends
    @GetMapping("/participants/{participantId}/automated-report")
    public AutomatedReportResponse getAutomatedReport(
        @PathVariable Long participantId,
        Authentication authentication
    ) {
        return clinicianService.generateAutomatedReport(authentication.getName(), participantId);
    }

    @GetMapping("/sessions/compare")
    public SessionCompareResponse compareSessions(
        @RequestParam Long sessionA,
        @RequestParam Long sessionB,
        Authentication authentication
    ) {
        return clinicianService.compareSessions(authentication.getName(), sessionA, sessionB);
    }

    @PostMapping("/sessions/compare-multi")
    public MultiSessionCompareResponse compareMultipleSessions(
        Authentication authentication,
        @RequestBody MultiSessionCompareRequest request
    ) {
        return clinicianService.compareMultipleSessions(authentication.getName(), request.sessionIds());
    }

    @GetMapping("/sessions/{sessionId}/results")
    public SessionResultResponse getSessionResult(
        @PathVariable Long sessionId,
        Authentication authentication
    ) {
        return clinicianService.getSessionResult(authentication.getName(), sessionId);
    }

    /// CSV download with SHA-256 anonymized participant ids
    @PostMapping("/export/cognitive")
    public ResponseEntity<byte[]> exportCognitiveMetrics(
        Authentication authentication,
        @RequestBody(required = false) CognitiveExportRequest request
    ) {
        String csv = clinicianService.exportAnonymizedCognitiveMetrics(authentication.getName(), request);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cognitive-metrics-anonymized.csv\"")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(bytes);
    }
}
