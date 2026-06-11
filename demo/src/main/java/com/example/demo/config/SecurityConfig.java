package com.example.demo.config;

import com.example.demo.security.JwtFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// who can hit which URL + CORS for React on localhost:5173
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final List<String> allowedOriginPatterns;

    /// spring injects JwtFilter and reads allowed frontend URLs from properties
    public SecurityConfig(
        JwtFilter jwtFilter,
        @Value("${app.cors.allowed-origin-patterns:http://localhost:5173}") String allowedOriginPatterns
    ) {
        this.jwtFilter = jwtFilter;
        this.allowedOriginPatterns = List.of(allowedOriginPatterns.split(","));
    }

    @Bean
    /// creates the main Spring Security configuration object
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // uses corsConfigurationSource() below
            .csrf(csrf -> csrf.disable()) // ok for stateless JWT API
            .authorizeHttpRequests(auth -> auth
                    // login/register don't need a token
                    .requestMatchers("/", "/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                    .requestMatchers("/api/clinician/**").hasRole("CLINICIAN")
                    .requestMatchers("/api/research/**").hasAnyRole("USER", "CLINICIAN")
                    .requestMatchers("/api/user/**").hasAnyRole("USER", "CLINICIAN")
                    .requestMatchers("/api/sessions/**").hasAnyRole("USER", "CLINICIAN")
                    .requestMatchers("/api/session/**").hasAnyRole("USER", "CLINICIAN")
                    .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            ) // no server session — JWT on every request
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    /// lets React send cookies cross-origin (withCredentials: true)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
