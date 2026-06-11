package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.CurrentUserResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.security.AuthCookieFactory;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
// REST endpoints for register / login / logout — thin layer, logic is in AuthService
public class AuthController {

    private final AuthService authService;

    /// spring injects the auth service
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /// POST /api/auth/register — create new user account
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// POST /api/auth/login — check password, return JSON + Set-Cookie with JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return authService.loginUser(request);
    }

    /// POST /api/auth/refresh — new access JWT when old one expired (uses refresh cookie)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        return authService.refreshSession(resolveCookie(request, AuthCookieFactory.REFRESH_COOKIE));
    }

    /// POST /api/auth/logout — kill tokens, clear cookies
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return authService.logoutUser(email, resolveCookie(request, AuthCookieFactory.REFRESH_COOKIE));
    }

    /// GET /api/auth/me — who am I? (needs valid JWT in cookie)
    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }

    /// reads a cookie value by name from the incoming request
    private String resolveCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
