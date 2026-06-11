package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.CurrentUserResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthCookieFactory;
import com.example.demo.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// register, login, logout, refresh — passwords hashed with BCrypt, tokens in cookies
public class AuthService {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "CLINICIAN");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthCookieFactory authCookieFactory;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;

    /// wires up repos + jwt + cookie factory from application.properties
    public AuthService(
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        JwtService jwtService,
        AuthCookieFactory authCookieFactory,
        @Value("${jwt.expiration}") long jwtExpirationMs,
        @Value("${jwt.refresh-expiration}") long refreshExpirationMs
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.authCookieFactory = authCookieFactory;
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /// new account — hash password, save to users table
    public String registerUser(RegisterRequest request) {
        validateRegisterRequest(request);

        String normalizedRole = request.getRole().trim().toUpperCase();

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new ConflictException("Email already registered.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail(), encodedPassword, normalizedRole);
        userRepository.save(user);

        return "User registered successfully.";
    }

    /// login — find user, BCrypt check, then issue JWT cookies
    @Transactional
    public ResponseEntity<AuthResponse> loginUser(LoginRequest request) {
        validateLoginRequest(request);

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Wrong email or password."));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordMatches) {
            throw new UnauthorizedException("Wrong email or password.");
        }

        return buildAuthResponse(user);
    }

    /// when access JWT expired — swap refresh cookie for new JWT pair
    @Transactional
    public ResponseEntity<AuthResponse> refreshSession(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing.");
        }

        RefreshToken refreshToken = refreshTokenRepository
            .findByTokenHashAndRevokedFalse(hashToken(refreshTokenValue))
            .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid."));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            throw new UnauthorizedException("Refresh token has expired.");
        }

        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        return buildAuthResponse(user);
    }

    /// logout — bump tokenVersion so old JWTs die, delete refresh tokens, clear cookies
    @Transactional
    public ResponseEntity<Void> logoutUser(String email, String refreshTokenValue) {
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(user -> {
                user.incrementTokenVersion();
                userRepository.save(user);
                refreshTokenRepository.deleteByUser(user);
            });
        } else if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenRepository.findByTokenHashAndRevokedFalse(hashToken(refreshTokenValue))
                .ifPresent(token -> token.setRevoked(true));
        }

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, authCookieFactory.clearAccessCookie().toString())
            .header(HttpHeaders.SET_COOKIE, authCookieFactory.clearRefreshCookie().toString())
            .build();
    }

    /// used by GET /api/auth/me — return email + role for frontend
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user was not found."));
        return new CurrentUserResponse(user.getEmail(), user.getRole());
    }

    /// shared by login + refresh — create JWT, refresh token, set both cookies
    private ResponseEntity<AuthResponse> buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(
            user.getEmail(),
            user.getRole(),
            user.getTokenVersion() == null ? 0L : user.getTokenVersion()
        );
        String refreshTokenValue = createRefreshToken(user);

        ResponseCookie accessCookie = authCookieFactory.accessCookie(accessToken, jwtExpirationMs / 1000);
        ResponseCookie refreshCookie = authCookieFactory.refreshCookie(refreshTokenValue, refreshExpirationMs / 1000);

        AuthResponse body = new AuthResponse(user.getEmail(), user.getRole());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(body);
    }

    /// store hashed refresh token in DB, return raw token for cookie only
    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        String rawToken = generateRawToken();
        RefreshToken refreshToken = new RefreshToken(
            user,
            hashToken(rawToken),
            LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000)
        );
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    /// random 32 bytes for refresh token
    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /// SHA-256 before saving refresh token — if DB leaks, hashes are useless
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new BadRequestException("Unable to process refresh token.");
        }
    }

    /// register form validation + password rules
    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new BadRequestException("Missing body.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password required.");
        }
        validatePasswordStrength(request.getPassword());
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new BadRequestException("Role required.");
        }

        String normalizedRole = request.getRole().trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("Role must be USER or CLINICIAN.");
        }
    }

    /// login form — just need email + password present
    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new BadRequestException("Missing body.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password required.");
        }
    }

    /// min 8 chars, upper, lower, digit — same rules as frontend
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new BadRequestException("Password too short (min 8).");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Need an uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Need a lowercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Need a number in password.");
        }
    }
}
