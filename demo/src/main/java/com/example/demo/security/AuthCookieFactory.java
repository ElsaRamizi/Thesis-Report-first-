package com.example.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
// builds Set-Cookie headers for login/logout — httpOnly so JS can't steal tokens
public class AuthCookieFactory {

    public static final String REFRESH_COOKIE = "mm_refresh_token";

    private final boolean secureCookies;

    /// secure=true in prod (HTTPS only), false locally
    public AuthCookieFactory(@Value("${app.cookie.secure:false}") boolean secureCookies) {
        this.secureCookies = secureCookies;
    }

    /// short-lived JWT cookie — sent on every API request (path /)
    public ResponseCookie accessCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(JwtFilter.ACCESS_COOKIE, token)
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite("Lax")
            .path("/")
            .maxAge(maxAgeSeconds)
            .build();
    }

    /// long-lived refresh cookie — only sent to /api/auth/* endpoints
    public ResponseCookie refreshCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(maxAgeSeconds)
            .build();
    }

    /// logout — overwrite access cookie with empty value, maxAge 0
    public ResponseCookie clearAccessCookie() {
        return ResponseCookie.from(JwtFilter.ACCESS_COOKIE, "")
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();
    }

    /// same for refresh cookie
    public ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "")
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(0)
            .build();
    }
}
