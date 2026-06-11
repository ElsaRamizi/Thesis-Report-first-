package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.DoctorConnection;
import com.example.demo.model.ParticipantProfile;
import com.example.demo.model.ResearchParticipation;
import com.example.demo.model.ResearchStudy;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorConnectionRepository;
import com.example.demo.repository.ParticipantProfileRepository;
import com.example.demo.repository.ResearchParticipationRepository;
import com.example.demo.repository.ResearchStudyRepository;
import com.example.demo.repository.UserRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// decides which participants a clinician is allowed to see — privacy gate for all clinician features
public class ClinicianAccessService {

    private final UserRepository userRepository;
    private final ParticipantProfileRepository profileRepository;
    private final DoctorConnectionRepository doctorConnectionRepository;
    private final ResearchStudyRepository studyRepository;
    private final ResearchParticipationRepository participationRepository;

    public ClinicianAccessService(
        UserRepository userRepository,
        ParticipantProfileRepository profileRepository,
        DoctorConnectionRepository doctorConnectionRepository,
        ResearchStudyRepository studyRepository,
        ResearchParticipationRepository participationRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.doctorConnectionRepository = doctorConnectionRepository;
        this.studyRepository = studyRepository;
        this.participationRepository = participationRepository;
    }

    /// load user and make sure role is CLINICIAN
    @Transactional(readOnly = true)
    public User findClinician(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User was not found."));
        if (!"CLINICIAN".equals(user.getRole())) {
            throw new UnauthorizedException("Clinician access is required.");
        }
        return user;
    }

    /// merge 3 sources: assigned on profile, doctor connection consent, research study sharing
    @Transactional(readOnly = true)
    public List<User> getAccessibleParticipants(String clinicianEmail) {
        User clinician = findClinician(clinicianEmail);
        Map<Long, User> participants = new LinkedHashMap<>();

        for (ParticipantProfile profile : profileRepository.findByAssignedClinician(clinician)) {
            participants.putIfAbsent(profile.getUser().getId(), profile.getUser());
        }

        for (DoctorConnection connection : doctorConnectionRepository
            .findByDoctorEmailIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(clinician.getEmail())) {
            if (hasSharingConsent(connection)) {
                participants.putIfAbsent(connection.getPatient().getId(), connection.getPatient());
            }
        }

        for (ResearchStudy study : studyRepository.findByCreatorOrderByCreatedAtDesc(clinician)) {
            for (ResearchParticipation participation : participationRepository.findByStudyOrderByJoinedAtDesc(study)) {
                if (participation.isDataSharingAccepted()
                    && !ResearchService.PARTICIPATION_WITHDRAWN.equals(participation.getStatus())) {
                    participants.putIfAbsent(participation.getParticipant().getId(), participation.getParticipant());
                }
            }
        }

        return participants.values().stream()
            .sorted(Comparator.comparing(User::getEmail))
            .toList();
    }

    /// throw if clinician tries to open someone they shouldn't see
    @Transactional(readOnly = true)
    public User requireAccessibleParticipant(String clinicianEmail, Long participantId) {
        User participant = userRepository.findById(participantId)
            .orElseThrow(() -> new BadRequestException("Participant was not found."));
        if (!"USER".equals(participant.getRole())) {
            throw new BadRequestException("The selected account is not a participant.");
        }
        if (!canAccess(findClinician(clinicianEmail), participant)) {
            throw new UnauthorizedException("You do not have access to this participant.");
        }
        return participant;
    }

    /// simple check — is this participant in the accessible list?
    @Transactional(readOnly = true)
    public boolean canAccess(User clinician, User participant) {
        return getAccessibleParticipants(clinician.getEmail()).stream()
            .anyMatch(user -> user.getId().equals(participant.getId()));
    }

    /// patient ticked at least one sharing option on data sharing page
    private boolean hasSharingConsent(DoctorConnection connection) {
        return connection.isShareAnalyticsOnly()
            || connection.isShareFullIdentifiable()
            || connection.isShareAnonymizedOnly()
            || connection.isShareSelectedGamesOnly();
    }
}
