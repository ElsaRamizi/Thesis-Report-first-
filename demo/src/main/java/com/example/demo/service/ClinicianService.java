package com.example.demo.service;

import com.example.demo.dto.ClinicianParticipantResponse;
import com.example.demo.dto.ClinicianSessionSummaryResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.dto.TrialResultResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import com.example.demo.model.User;
import com.example.demo.repository.AggregatedMetricsRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.TrialDataRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicianService {

    private final UserRepository userRepository;
    private final TestSessionRepository testSessionRepository;
    private final TrialDataRepository trialDataRepository;
    private final AggregatedMetricsRepository aggregatedMetricsRepository;
    private final TaskCatalogService taskCatalogService;

    public ClinicianService(
        UserRepository userRepository,
        TestSessionRepository testSessionRepository,
        TrialDataRepository trialDataRepository,
        AggregatedMetricsRepository aggregatedMetricsRepository,
        TaskCatalogService taskCatalogService
    ) {
        this.userRepository = userRepository;
        this.testSessionRepository = testSessionRepository;
        this.trialDataRepository = trialDataRepository;
        this.aggregatedMetricsRepository = aggregatedMetricsRepository;
        this.taskCatalogService = taskCatalogService;
    }

    @Transactional(readOnly = true)
    public List<ClinicianParticipantResponse> getParticipants() {
        return userRepository.findByRoleOrderByCreatedAtDesc("USER").stream()
            .map(this::toParticipantResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicianSessionSummaryResponse> getParticipantSessions(Long participantId) {
        User participant = findParticipant(participantId);
        return testSessionRepository.findByUserIdOrderByStartTimeDesc(participant).stream()
            .map(this::toSessionSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public SessionResultResponse getSessionResult(Long sessionId) {
        TestSession session = testSessionRepository.findById(sessionId)
            .orElseThrow(() -> new BadRequestException("Session was not found."));

        if (!"USER".equals(session.getUserId().getRole())) {
            throw new BadRequestException("Only participant sessions can be viewed here.");
        }

        AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(session)
            .orElseThrow(() -> new BadRequestException("Metrics were not found for this session."));
        List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);

        return toResultResponse(session, metrics, trials);
    }

    private ClinicianParticipantResponse toParticipantResponse(User participant) {
        List<TestSession> sessions = testSessionRepository.findByUserIdOrderByStartTimeDesc(participant);
        TestSession latestSession = sessions.isEmpty() ? null : sessions.get(0);
        AggregatedMetrics latestMetrics = latestSession == null
            ? null
            : aggregatedMetricsRepository.findBySessionId(latestSession).orElse(null);

        return new ClinicianParticipantResponse(
            participant.getId(),
            participant.getEmail(),
            sessions.size(),
            latestSession != null ? latestSession.getStartTime() : null,
            latestMetrics != null ? latestMetrics.getAccuracy() : null,
            latestMetrics != null ? latestMetrics.getAvgReactionTime() : null
        );
    }

    private ClinicianSessionSummaryResponse toSessionSummary(TestSession session) {
        AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(session).orElse(null);
        return new ClinicianSessionSummaryResponse(
            session.getId(),
            session.getUserId().getId(),
            session.getUserId().getEmail(),
            session.getTaskType(),
            resolveTaskTitle(session.getTaskType()),
            session.getDifficultyLevel(),
            session.getStartTime(),
            session.getEndTime(),
            metrics != null ? metrics.getAvgReactionTime() : null,
            metrics != null ? metrics.getAccuracy() : null,
            metrics != null ? metrics.getErrorRate() : null,
            metrics != null ? metrics.getFalseAlarmRate() : null,
            metrics != null ? metrics.getMaxNReached() : null
        );
    }

    private SessionResultResponse toResultResponse(TestSession session, AggregatedMetrics metrics, List<TrialData> trials) {
        List<TrialResultResponse> trialResponses = trials.stream()
            .map(this::toTrialResponse)
            .toList();

        return new SessionResultResponse(
            session.getId(),
            session.getTaskType(),
            resolveTaskTitle(session.getTaskType()),
            session.getDifficultyLevel(),
            session.getStartTime(),
            session.getEndTime(),
            metrics.getAvgReactionTime(),
            metrics.getAccuracy(),
            metrics.getErrorRate(),
            metrics.getFalseAlarmRate(),
            metrics.getMaxNReached(),
            metrics.getDPrime(),
            trialResponses
        );
    }

    private TrialResultResponse toTrialResponse(TrialData trial) {
        return new TrialResultResponse(
            trial.getId(),
            trial.getStimulus(),
            trial.getResponse(),
            trial.isCorrect(),
            trial.getReactionTime(),
            trial.getTrialIndex(),
            trial.getNLevel(),
            trial.getPosition(),
            trial.getLetter(),
            trial.getExpectedPositionMatch(),
            trial.getExpectedLetterMatch(),
            trial.getUserPressedPosition(),
            trial.getUserPressedLetter(),
            trial.getPositionOutcome(),
            trial.getLetterOutcome(),
            trial.getReactionTimePosition(),
            trial.getReactionTimeLetter()
        );
    }

    private User findParticipant(Long participantId) {
        User participant = userRepository.findById(participantId)
            .orElseThrow(() -> new BadRequestException("Participant was not found."));

        if (!"USER".equals(participant.getRole())) {
            throw new BadRequestException("The selected account is not a participant.");
        }

        return participant;
    }

    private String resolveTaskTitle(String taskType) {
        try {
            return taskCatalogService.getTaskById(taskType).getTitle();
        } catch (BadRequestException ex) {
            return taskType;
        }
    }
}
