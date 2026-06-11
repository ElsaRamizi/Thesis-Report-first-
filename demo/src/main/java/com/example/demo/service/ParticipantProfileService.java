package com.example.demo.service;

import com.example.demo.dto.ParticipantProfileResponse;
import com.example.demo.dto.ParticipantProfileUpdateRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.ParticipantProfile;
import com.example.demo.model.User;
import com.example.demo.repository.ParticipantProfileRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantProfileService {

    private final ParticipantProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ClinicianAccessService clinicianAccessService;

    public ParticipantProfileService(
        ParticipantProfileRepository profileRepository,
        UserRepository userRepository,
        ClinicianAccessService clinicianAccessService
    ) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.clinicianAccessService = clinicianAccessService;
    }

    @Transactional
    public ParticipantProfileResponse getOwnProfile(String email) {
        User user = findUser(email);
        return toResponse(getOrCreateProfile(user));
    }

    @Transactional
    public ParticipantProfileResponse updateOwnProfile(String email, ParticipantProfileUpdateRequest request) {
        User user = findUser(email);
        if (!"USER".equals(user.getRole())) {
            throw new UnauthorizedException("Only participants can update their profile here.");
        }

        ParticipantProfile profile = getOrCreateProfile(user);
        if (request.dateOfBirth() != null) {
            profile.setDateOfBirth(request.dateOfBirth());
        }
        if (request.notes() != null) {
            profile.setNotes(request.notes());
        }
        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public ParticipantProfileResponse getParticipantProfile(String clinicianEmail, Long participantId) {
        User participant = clinicianAccessService.requireAccessibleParticipant(clinicianEmail, participantId);
        return toResponse(getOrCreateProfile(participant));
    }

    @Transactional
    public ParticipantProfileResponse updateParticipantProfile(
        String clinicianEmail,
        Long participantId,
        ParticipantProfileUpdateRequest request
    ) {
        User participant = clinicianAccessService.requireAccessibleParticipant(clinicianEmail, participantId);
        ParticipantProfile profile = getOrCreateProfile(participant);

        if (request.dateOfBirth() != null) {
            profile.setDateOfBirth(request.dateOfBirth());
        }
        if (request.notes() != null) {
            profile.setNotes(request.notes());
        }
        if (Boolean.TRUE.equals(request.clearAssignedClinician())) {
            profile.setAssignedClinician(null);
        } else if (request.assignedClinicianId() != null) {
            User clinician = userRepository.findById(request.assignedClinicianId())
                .orElseThrow(() -> new BadRequestException("Clinician was not found."));
            if (!"CLINICIAN".equals(clinician.getRole())) {
                throw new BadRequestException("Assigned user must be a clinician.");
            }
            profile.setAssignedClinician(clinician);
        }

        return toResponse(profileRepository.save(profile));
    }

    private ParticipantProfile getOrCreateProfile(User user) {
        return profileRepository.findByUser(user).orElseGet(() -> {
            ParticipantProfile profile = new ParticipantProfile();
            profile.setUser(user);
            return profileRepository.save(profile);
        });
    }

    private ParticipantProfileResponse toResponse(ParticipantProfile profile) {
        Integer age = profile.getDateOfBirth() == null
            ? null
            : Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears();

        User assigned = profile.getAssignedClinician();
        return new ParticipantProfileResponse(
            profile.getUser().getId(),
            profile.getUser().getEmail(),
            profile.getDateOfBirth(),
            age,
            profile.getNotes(),
            assigned != null ? assigned.getId() : null,
            assigned != null ? assigned.getEmail() : null
        );
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User was not found."));
    }

    private User findClinician(String email) {
        User user = findUser(email);
        if (!"CLINICIAN".equals(user.getRole())) {
            throw new UnauthorizedException("Clinician access is required.");
        }
        return user;
    }

    private User findParticipant(Long participantId) {
        User participant = userRepository.findById(participantId)
            .orElseThrow(() -> new BadRequestException("Participant was not found."));
        if (!"USER".equals(participant.getRole())) {
            throw new BadRequestException("The selected account is not a participant.");
        }
        return participant;
    }
}
