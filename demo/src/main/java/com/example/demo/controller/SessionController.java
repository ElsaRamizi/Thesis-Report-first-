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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(originPatterns = "*")
/**
 * Exposes endpoints for saving completed sessions and retrieving saved results.
 */
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/complete")
    /**
     * Persists a completed session for the authenticated user.
     *
     * @param request session payload
     * @param authentication current authenticated principal
     * @return saved session summary
     */
    public ResponseEntity<SessionResultResponse> completeSession(
        @RequestBody SessionCompleteRequest request,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.completeSession(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/latest")
    /**
     * Returns the latest saved session for the authenticated user.
     *
     * @param authentication current authenticated principal
     * @return latest session summary
     */
    public ResponseEntity<SessionResultResponse> getLatestSession(Authentication authentication) {
        SessionResultResponse response = sessionService.getLatestSessionResult(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    /**
     * Returns historical session summaries for the authenticated user.
     *
     * @param authentication current authenticated principal
     * @return session history list
     */
    public ResponseEntity<List<SessionHistoryItemResponse>> getSessionHistory(Authentication authentication) {
        List<SessionHistoryItemResponse> response = sessionService.getSessionHistory(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    /**
     * Starts a live session and returns its database id.
     *
     * @param request start payload
     * @param authentication current authenticated principal
     * @return created session id
     */
    public ResponseEntity<SessionStartResponse> startSession(
        @RequestBody SessionStartRequest request,
        Authentication authentication
    ) {
        SessionStartResponse response = sessionService.startSession(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/trial")
    /**
     * Persists one live trial for the authenticated user.
     *
     * @param request trial payload
     * @param authentication current authenticated principal
     * @return saved trial
     */
    public ResponseEntity<TrialResultResponse> recordTrial(
        @RequestBody TrialSubmitRequest request,
        Authentication authentication
    ) {
        TrialResultResponse response = sessionService.recordTrial(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/end")
    /**
     * Ends a live session and computes its metrics.
     *
     * @param request end payload
     * @param authentication current authenticated principal
     * @return session summary
     */
    public ResponseEntity<SessionResultResponse> endSession(
        @RequestBody SessionEndRequest request,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.endSession(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/metrics")
    /**
     * Returns persisted metrics for one session.
     *
     * @param sessionId session identifier
     * @param authentication current authenticated principal
     * @return session metrics
     */
    public ResponseEntity<SessionResultResponse> getSessionMetrics(
        @PathVariable Long sessionId,
        Authentication authentication
    ) {
        SessionResultResponse response = sessionService.getSessionMetrics(authentication.getName(), sessionId);
        return ResponseEntity.ok(response);
    }
}
