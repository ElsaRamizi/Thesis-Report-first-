package com.example.demo.service;

import com.example.demo.dto.AnnotationRequest;
import com.example.demo.dto.AnnotationResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.ClinicianAnnotation;
import com.example.demo.model.TestSession;
import com.example.demo.model.User;
import com.example.demo.repository.ClinicianAnnotationRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicianAnnotationService {

    private final ClinicianAnnotationRepository annotationRepository;
    private final UserRepository userRepository;
    private final TestSessionRepository testSessionRepository;
    private final ClinicianAccessService clinicianAccessService;

    public ClinicianAnnotationService(
        ClinicianAnnotationRepository annotationRepository,
        UserRepository userRepository,
        TestSessionRepository testSessionRepository,
        ClinicianAccessService clinicianAccessService
    ) {
        this.annotationRepository = annotationRepository;
        this.userRepository = userRepository;
        this.testSessionRepository = testSessionRepository;
        this.clinicianAccessService = clinicianAccessService;
    }

    @Transactional(readOnly = true)
    public List<AnnotationResponse> getParticipantAnnotations(String clinicianEmail, Long participantId) {
        User participant = clinicianAccessService.requireAccessibleParticipant(clinicianEmail, participantId);
        return annotationRepository.findByParticipantOrderByCreatedAtDesc(participant).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AnnotationResponse addAnnotation(String clinicianEmail, Long participantId, AnnotationRequest request) {
        User clinician = clinicianAccessService.findClinician(clinicianEmail);
        User participant = clinicianAccessService.requireAccessibleParticipant(clinicianEmail, participantId);

        if (request.content() == null || request.content().isBlank()) {
            throw new BadRequestException("Annotation content is required.");
        }

        TestSession session = null;
        if (request.sessionId() != null) {
            session = testSessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new BadRequestException("Session was not found."));
            if (!session.getUserId().getId().equals(participant.getId())) {
                throw new BadRequestException("Session does not belong to this participant.");
            }
        }

        ClinicianAnnotation annotation = new ClinicianAnnotation();
        annotation.setClinician(clinician);
        annotation.setParticipant(participant);
        annotation.setSession(session);
        annotation.setContent(request.content().trim());
        annotation.setCreatedAt(LocalDateTime.now());

        return toResponse(annotationRepository.save(annotation));
    }

    private AnnotationResponse toResponse(ClinicianAnnotation annotation) {
        return new AnnotationResponse(
            annotation.getId(),
            annotation.getParticipant().getId(),
            annotation.getSession() != null ? annotation.getSession().getId() : null,
            annotation.getClinician().getEmail(),
            annotation.getContent(),
            annotation.getCreatedAt()
        );
    }
}
