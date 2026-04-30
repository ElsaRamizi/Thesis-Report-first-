package com.example.demo.controller;

import com.example.demo.dto.SessionEndRequest;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.dto.SessionStartRequest;
import com.example.demo.dto.SessionStartResponse;
import com.example.demo.dto.TrialResultResponse;
import com.example.demo.dto.TrialSubmitRequest;
import com.example.demo.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
@CrossOrigin(originPatterns = "*")
/**
 * Provides the thesis-specified singular session API paths.
 */
public class SessionAliasController {

    private final SessionService sessionService;

    public SessionAliasController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<SessionStartResponse> startSession(
        @RequestBody SessionStartRequest request,
        Authentication authentication
    ) {
        SessionStartResponse response = sessionService.startSession(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/trial")
    public ResponseEntity<TrialResultResponse> recordTrial(
        @RequestBody TrialSubmitRequest request,
        Authentication authentication
    ) {
        TrialResultResponse response = sessionService.recordTrial(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/end")
    public ResponseEntity<SessionResultResponse> endSession(
        @RequestBody SessionEndRequest request,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.endSession(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/metrics")
    public ResponseEntity<SessionResultResponse> getSessionMetrics(
        @PathVariable Long sessionId,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.getSessionMetrics(authentication.getName(), sessionId);
        return ResponseEntity.ok(response);
    }
}
