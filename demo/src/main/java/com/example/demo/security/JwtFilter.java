package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
// runs before every controller — reads JWT cookie and tells Spring who is logged in
public class JwtFilter extends OncePerRequestFilter {

    public static final String ACCESS_COOKIE = "mm_access_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /// constructor injection
    public JwtFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /// main filter — runs once per HTTP request, before controllers
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            String email = jwtService.extractEmail(token);

            // only set auth if nobody logged in yet on this request
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    if (jwtService.isTokenValid(token, user.getEmail(), user.getTokenVersion())) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                                );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }

        // always continue — if token bad, protected routes will 401 later
        filterChain.doFilter(request, response);
    }

    /// find JWT in cookie first, then Authorization Bearer header (for Postman etc)
    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ACCESS_COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
