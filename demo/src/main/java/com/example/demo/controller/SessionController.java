package com.example.demo.controller;

import com.example.demo.dto.SessionCompleteRequest;
import com.example.demo.dto.SessionHistoryItemResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.service.SessionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
}
