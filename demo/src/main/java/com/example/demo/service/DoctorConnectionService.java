package com.example.demo.service;

import com.example.demo.dto.DoctorConnectionRequest;
import com.example.demo.dto.DoctorConnectionResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.DoctorConnection;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorConnectionRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorConnectionService {

    private final DoctorConnectionRepository doctorConnectionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DoctorConnectionService(
        DoctorConnectionRepository doctorConnectionRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper
    ) {
        this.doctorConnectionRepository = doctorConnectionRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DoctorConnectionResponse> getPatientConnections(String patientEmail) {
        User patient = findPatient(patientEmail);
        return doctorConnectionRepository.findByPatientOrderByCreatedAtDesc(patient).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public DoctorConnectionResponse createConnection(String patientEmail, DoctorConnectionRequest request) {
        User patient = findPatient(patientEmail);
        validateRequest(request);

        DoctorConnection connection = new DoctorConnection();
        connection.setPatient(patient);
        connection.setDoctorName(request.doctorName().trim());
        connection.setDoctorSurname(request.doctorSurname().trim());
        connection.setDoctorEmail(request.doctorEmail().trim().toLowerCase(Locale.ROOT));
        connection.setInstitution(request.institution());
        connection.setSpecialization(request.specialization());
        connection.setShareFullIdentifiable(request.shareFullIdentifiable());
        connection.setShareAnonymizedOnly(request.shareAnonymizedOnly());
        connection.setShareSelectedGamesOnly(request.shareSelectedGamesOnly());
        connection.setShareQuestionnaires(request.shareQuestionnaires());
        connection.setShareAnalyticsOnly(request.shareAnalyticsOnly());
        connection.setSelectedGamesJson(toGamesJson(request.selectedGames()));
        connection.setUseAnonymousSharing(request.useAnonymousSharing());
        connection.setAnonymousIdentifier(request.useAnonymousSharing()
            ? "SHARE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
            : null);
        connection.setActive(true);
        connection.setConsentAcceptedAt(LocalDateTime.now());
        connection.setCreatedAt(LocalDateTime.now());

        return toResponse(doctorConnectionRepository.save(connection));
    }

    @Transactional
    public DoctorConnectionResponse revokeConnection(String patientEmail, Long connectionId) {
        User patient = findPatient(patientEmail);
        DoctorConnection connection = doctorConnectionRepository.findByIdAndPatient(connectionId, patient)
            .orElseThrow(() -> new BadRequestException("Doctor connection was not found."));
        connection.setActive(false);
        connection.setRevokedAt(LocalDateTime.now());
        return toResponse(doctorConnectionRepository.save(connection));
    }

    @Transactional
    public DoctorConnectionResponse reactivateConnection(String patientEmail, Long connectionId) {
        User patient = findPatient(patientEmail);
        DoctorConnection connection = doctorConnectionRepository.findByIdAndPatient(connectionId, patient)
            .orElseThrow(() -> new BadRequestException("Doctor connection was not found."));
        connection.setActive(true);
        connection.setRevokedAt(null);
        return toResponse(doctorConnectionRepository.save(connection));
    }

    private void validateRequest(DoctorConnectionRequest request) {
        if (request.doctorName() == null || request.doctorName().isBlank()) {
            throw new BadRequestException("Doctor name is required.");
        }
        if (request.doctorSurname() == null || request.doctorSurname().isBlank()) {
            throw new BadRequestException("Doctor surname is required.");
        }
        if (request.doctorEmail() == null || request.doctorEmail().isBlank()) {
            throw new BadRequestException("Doctor email is required.");
        }
        if (!request.consentAccepted()) {
            throw new BadRequestException("Explicit consent is required before sharing any data.");
        }
        if (!request.shareFullIdentifiable()
            && !request.shareAnonymizedOnly()
            && !request.shareAnalyticsOnly()) {
            throw new BadRequestException("Select at least one sharing mode.");
        }
    }

    private User findPatient(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User was not found."));
        if (!"USER".equals(user.getRole())) {
            throw new UnauthorizedException("Only participants can manage doctor connections.");
        }
        return user;
    }

    private DoctorConnectionResponse toResponse(DoctorConnection connection) {
        return new DoctorConnectionResponse(
            connection.getId(),
            connection.getDoctorName(),
            connection.getDoctorSurname(),
            connection.getDoctorEmail(),
            connection.getInstitution(),
            connection.getSpecialization(),
            connection.isShareFullIdentifiable(),
            connection.isShareAnonymizedOnly(),
            connection.isShareSelectedGamesOnly(),
            connection.isShareQuestionnaires(),
            connection.isShareAnalyticsOnly(),
            parseGames(connection.getSelectedGamesJson()),
            connection.isUseAnonymousSharing(),
            connection.getAnonymousIdentifier(),
            connection.isActive(),
            connection.getConsentAcceptedAt(),
            connection.getRevokedAt(),
            connection.getCreatedAt()
        );
    }

    private String toGamesJson(List<String> games) {
        if (games == null || games.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(games);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Selected games could not be saved.");
        }
    }

    private List<String> parseGames(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }
}
