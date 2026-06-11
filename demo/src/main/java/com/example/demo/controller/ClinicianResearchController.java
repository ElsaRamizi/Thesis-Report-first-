package com.example.demo.controller;

import com.example.demo.dto.ResearchAnalyticsResponse;
import com.example.demo.dto.ResearchCohortRequest;
import com.example.demo.dto.ResearchCohortResponse;
import com.example.demo.dto.ResearchCompareRequest;
import com.example.demo.dto.ResearchCompareResponse;
import com.example.demo.dto.ResearchFilterPreviewRequest;
import com.example.demo.dto.ResearchParticipantSummary;
import com.example.demo.dto.ResearchQuestionRequest;
import com.example.demo.dto.ResearchStudyDetailResponse;
import com.example.demo.dto.ResearchStudyRequest;
import com.example.demo.dto.ResearchStudyResponse;
import com.example.demo.service.ResearchService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinician/research")
public class ClinicianResearchController {

    private final ResearchService researchService;

    public ClinicianResearchController(ResearchService researchService) {
        this.researchService = researchService;
    }

    @GetMapping("/studies")
    public List<ResearchStudyResponse> getStudies(Authentication authentication) {
        return researchService.getClinicianStudies(authentication.getName());
    }

    @GetMapping("/studies/{studyId}")
    public ResearchStudyDetailResponse getStudy(@PathVariable Long studyId, Authentication authentication) {
        return researchService.getClinicianStudyDetail(studyId, authentication.getName());
    }

    @PostMapping("/studies")
    public ResponseEntity<ResearchStudyDetailResponse> createStudy(
        Authentication authentication,
        @RequestBody ResearchStudyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(researchService.createStudy(authentication.getName(), request));
    }

    @PutMapping("/studies/{studyId}")
    public ResearchStudyDetailResponse updateStudy(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchStudyRequest request
    ) {
        return researchService.updateStudy(studyId, authentication.getName(), request);
    }

    @PostMapping("/studies/{studyId}/publish")
    public ResearchStudyDetailResponse publishStudy(@PathVariable Long studyId, Authentication authentication) {
        return researchService.publishStudy(studyId, authentication.getName());
    }

    @DeleteMapping("/studies/{studyId}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Long studyId, Authentication authentication) {
        researchService.deleteStudy(studyId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/studies/{studyId}/questions")
    public ResearchStudyDetailResponse addQuestion(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchQuestionRequest request
    ) {
        return researchService.addQuestion(studyId, authentication.getName(), request);
    }

    @PutMapping("/questions/{questionId}")
    public ResearchStudyDetailResponse updateQuestion(
        @PathVariable Long questionId,
        Authentication authentication,
        @RequestBody ResearchQuestionRequest request
    ) {
        return researchService.updateQuestion(questionId, authentication.getName(), request);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResearchStudyDetailResponse deleteQuestion(
        @PathVariable Long questionId,
        Authentication authentication
    ) {
        return researchService.deleteQuestion(questionId, authentication.getName());
    }

    @GetMapping("/studies/{studyId}/cohorts")
    public List<ResearchCohortResponse> getCohorts(@PathVariable Long studyId, Authentication authentication) {
        return researchService.getCohorts(studyId, authentication.getName());
    }

    @PostMapping("/studies/{studyId}/cohorts")
    public ResponseEntity<ResearchCohortResponse> createCohort(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchCohortRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(researchService.createCohort(studyId, authentication.getName(), request));
    }

    @PostMapping("/studies/{studyId}/cohorts/preview")
    public List<ResearchParticipantSummary> previewFilter(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchFilterPreviewRequest request
    ) {
        return researchService.previewFilter(studyId, authentication.getName(), request);
    }

    @PostMapping("/studies/{studyId}/analytics")
    public ResearchAnalyticsResponse getAnalytics(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchFilterPreviewRequest request
    ) {
        return researchService.getAnalytics(studyId, authentication.getName(), request.filters());
    }

    @PostMapping("/studies/{studyId}/analytics/compare")
    public ResearchCompareResponse compareCohorts(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchCompareRequest request
    ) {
        return researchService.compareCohorts(studyId, authentication.getName(), request);
    }

    @PostMapping("/studies/{studyId}/export")
    public ResponseEntity<byte[]> exportData(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchFilterPreviewRequest request
    ) {
        String csv = researchService.exportAnonymizedData(studyId, authentication.getName(), request.filters());
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"research-export-" + studyId + ".csv\"")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(bytes);
    }
}
