package com.example.demo.controller;

import com.example.demo.dto.ResearchJoinRequest;
import com.example.demo.dto.ResearchParticipationResponse;
import com.example.demo.dto.ResearchStudyDetailResponse;
import com.example.demo.dto.ResearchStudyResponse;
import com.example.demo.dto.ResearchAnswerRequest;
import com.example.demo.service.ResearchService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/research")
public class ResearchController {

    private final ResearchService researchService;

    public ResearchController(ResearchService researchService) {
        this.researchService = researchService;
    }

    @GetMapping("/studies")
    public List<ResearchStudyResponse> browseStudies() {
        return researchService.browsePublishedStudies();
    }

    @GetMapping("/studies/{studyId}")
    public ResearchStudyDetailResponse getStudy(@PathVariable Long studyId) {
        return researchService.getPublishedStudyDetail(studyId);
    }

    @GetMapping("/participations")
    public List<ResearchParticipationResponse> getMyParticipations(Authentication authentication) {
        return researchService.getMyParticipations(authentication.getName());
    }

    @PostMapping("/studies/{studyId}/join")
    public ResponseEntity<ResearchParticipationResponse> joinStudy(
        @PathVariable Long studyId,
        Authentication authentication,
        @RequestBody ResearchJoinRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(researchService.joinStudy(studyId, authentication.getName(), request));
    }

    @PostMapping("/participations/{participationId}/withdraw")
    public ResearchParticipationResponse withdraw(
        @PathVariable Long participationId,
        Authentication authentication
    ) {
        return researchService.withdrawParticipation(participationId, authentication.getName());
    }

    @PutMapping("/participations/{participationId}/answers")
    public ResearchParticipationResponse updateAnswers(
        @PathVariable Long participationId,
        Authentication authentication,
        @RequestBody List<ResearchAnswerRequest> answers
    ) {
        return researchService.updateParticipationAnswers(participationId, authentication.getName(), answers);
    }
}
