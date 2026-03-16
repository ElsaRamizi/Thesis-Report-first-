package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
/**
 * Handles registration and login workflows for application users.
 */
public class AuthService {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "CLINICIAN");

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Registers a new user after validating the request payload.
     *
     * @param request registration payload
     * @return success message for the client
     */
    public String registerUser(RegisterRequest request) {
        validateRegisterRequest(request);

        String normalizedRole = request.getRole().trim().toUpperCase();

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new ConflictException("A user with this email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail(), encodedPassword, normalizedRole);
        userRepository.save(user);

        return "User registered successfully.";
    }

    /**
     * Authenticates a user and returns a signed JWT on success.
     *
     * @param request login credentials
     * @return token and role information for the authenticated user
     */
    public AuthResponse loginUser(LoginRequest request) {
        validateLoginRequest(request);

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordMatches) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getRole());
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required.");
        }
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new BadRequestException("Role is required.");
        }

        String normalizedRole = request.getRole().trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("Role must be USER or CLINICIAN.");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required.");
        }
    }
}
