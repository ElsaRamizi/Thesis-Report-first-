package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*")
/**
 * Exposes public authentication endpoints for registration and login.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    /**
     * Registers a new application user.
     *
     * @param request registration payload
     * @return success message with HTTP 201 on success
     */
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    /**
     * Authenticates a user and returns a JWT response payload.
     *
     * @param request login payload
     * @return signed token and role data
     */
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }
}
