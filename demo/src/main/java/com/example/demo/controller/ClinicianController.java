package com.example.demo.controller;

import com.example.demo.dto.ClinicianParticipantResponse;
import com.example.demo.dto.ClinicianSessionSummaryResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.service.ClinicianService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinician")
public class ClinicianController {

    private final ClinicianService clinicianService;

    public ClinicianController(ClinicianService clinicianService) {
        this.clinicianService = clinicianService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Protected clinician dashboard endpoint";
    }

    @GetMapping("/participants")
    public List<ClinicianParticipantResponse> getParticipants() {
        return clinicianService.getParticipants();
    }

    @GetMapping("/participants/{participantId}/sessions")
    public List<ClinicianSessionSummaryResponse> getParticipantSessions(@PathVariable Long participantId) {
        return clinicianService.getParticipantSessions(participantId);
    }

    @GetMapping("/sessions/{sessionId}/results")
    public SessionResultResponse getSessionResult(@PathVariable Long sessionId) {
        return clinicianService.getSessionResult(sessionId);
    }
}
