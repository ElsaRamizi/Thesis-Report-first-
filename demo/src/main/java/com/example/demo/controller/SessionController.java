package com.example.demo.controller;

import com.example.demo.dto.SessionCompleteRequest;
import com.example.demo.dto.SessionEndRequest;
import com.example.demo.dto.SessionHistoryItemResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.dto.SessionStartRequest;
import com.example.demo.dto.SessionStartResponse;
import com.example.demo.dto.TrialResultResponse;
import com.example.demo.dto.TrialSubmitRequest;
import com.example.demo.service.SessionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
// game session API — Stroop/Memory Span use /complete, N-Back can use these too
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /// Stroop + Memory Span — save whole session + all trials in one POST
    @PostMapping("/complete")
    public ResponseEntity<SessionResultResponse> completeSession(
        @RequestBody SessionCompleteRequest request,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.completeSession(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// dashboard / results page — most recent session with metrics
    @GetMapping("/latest")
    public ResponseEntity<SessionResultResponse> getLatestSession(Authentication authentication) {
        SessionResultResponse response = sessionService.getLatestSessionResult(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /// session history list for participant
    @GetMapping
    public ResponseEntity<List<SessionHistoryItemResponse>> getSessionHistory(Authentication authentication) {
        List<SessionHistoryItemResponse> response = sessionService.getSessionHistory(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /// start live session (also on /api/session/start via alias controller)
    @PostMapping("/start")
    public ResponseEntity<SessionStartResponse> startSession(
        @RequestBody SessionStartRequest request,
        Authentication authentication
    ) {
        SessionStartResponse response = sessionService.startSession(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// save one trial during Dual N-Back
    @PostMapping("/trial")
    public ResponseEntity<TrialResultResponse> recordTrial(
        @RequestBody TrialSubmitRequest request,
        Authentication authentication
    ) {
        TrialResultResponse response = sessionService.recordTrial(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// finish live session — compute aggregated metrics
    @PostMapping("/end")
    public ResponseEntity<SessionResultResponse> endSession(
        @RequestBody SessionEndRequest request,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.endSession(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    /// one session detail with trials + metrics
    @GetMapping("/{sessionId}/metrics")
    public ResponseEntity<SessionResultResponse> getSessionMetrics(
        @PathVariable Long sessionId,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.getSessionMetrics(authentication.getName(), sessionId);
        return ResponseEntity.ok(response);
    }
}
