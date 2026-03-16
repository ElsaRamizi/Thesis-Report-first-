package com.example.demo.service;

import com.example.demo.dto.SessionCompleteRequest;
import com.example.demo.dto.SessionHistoryItemResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.dto.TrialDataRequest;
import com.example.demo.dto.TrialResultResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import com.example.demo.model.User;
import com.example.demo.repository.AggregatedMetricsRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.TrialDataRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/**
 * Persists completed cognitive test sessions and exposes saved results.
 */
public class SessionService {

    private final UserRepository userRepository;
    private final TestSessionRepository testSessionRepository;
    private final TrialDataRepository trialDataRepository;
    private final AggregatedMetricsRepository aggregatedMetricsRepository;
    private final TaskCatalogService taskCatalogService;

    public SessionService(
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

    @Transactional
    /**
     * Saves a completed session, its trial data, and aggregated metrics for the authenticated user.
     *
     * @param email authenticated user email
     * @param request completed session payload
     * @return persisted session summary
     */
    public SessionResultResponse completeSession(String email, SessionCompleteRequest request) {
        validateRequest(request);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user was not found."));

        TestSession session = new TestSession(
            user,
            request.getTaskType(),
            request.getStartTime(),
            request.getEndTime(),
            request.getDifficultyLevel()
        );
        TestSession savedSession = testSessionRepository.save(session);

        List<TrialData> savedTrials = request.getTrials().stream()
            .map(trial -> trialDataRepository.save(toTrialData(savedSession, trial)))
            .toList();

        AggregatedMetrics metrics = aggregatedMetricsRepository.save(buildMetrics(savedSession, savedTrials));

        return toResponse(savedSession, metrics, savedTrials);
    }

    @Transactional(readOnly = true)
    /**
     * Loads the most recent saved session for the authenticated user.
     *
     * @param email authenticated user email
     * @return latest session summary
     */
    public SessionResultResponse getLatestSessionResult(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user was not found."));

        TestSession session = testSessionRepository.findTopByUserIdOrderByStartTimeDesc(user)
            .orElseThrow(() -> new BadRequestException("No saved sessions were found for this user."));

        AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(session)
            .orElseThrow(() -> new BadRequestException("Metrics were not found for the latest session."));

        List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);
        return toResponse(session, metrics, trials);
    }

    @Transactional(readOnly = true)
    /**
     * Returns the saved session history for the authenticated user.
     *
     * @param email authenticated user email
     * @return list of historical session summaries ordered from newest to oldest
     */
    public List<SessionHistoryItemResponse> getSessionHistory(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user was not found."));

        List<TestSession> sessions = testSessionRepository.findByUserIdOrderByStartTimeDesc(user);
        List<SessionHistoryItemResponse> history = new ArrayList<>();

        for (TestSession session : sessions) {
            aggregatedMetricsRepository.findBySessionId(session).ifPresent(metrics ->
                history.add(new SessionHistoryItemResponse(
                    session.getId(),
                    session.getTaskType(),
                    resolveTaskTitle(session.getTaskType()),
                    session.getDifficultyLevel(),
                    session.getStartTime(),
                    session.getEndTime(),
                    metrics.getAvgReactionTime(),
                    metrics.getAccuracy(),
                    metrics.getErrorRate()
                ))
            );
        }

        return history;
    }

    private void validateRequest(SessionCompleteRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        if (request.getTaskType() == null || request.getTaskType().isBlank()) {
            throw new BadRequestException("Task type is required.");
        }
        if (request.getDifficultyLevel() == null || request.getDifficultyLevel().isBlank()) {
            throw new BadRequestException("Difficulty level is required.");
        }
        if (request.getStartTime() == null) {
            throw new BadRequestException("Start time is required.");
        }
        if (request.getEndTime() == null) {
            throw new BadRequestException("End time is required.");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time.");
        }
        if (request.getTrials() == null || request.getTrials().isEmpty()) {
            throw new BadRequestException("At least one trial is required.");
        }
    }

    private TrialData toTrialData(TestSession session, TrialDataRequest request) {
        if (request.getStimulus() == null || request.getStimulus().isBlank()) {
            throw new BadRequestException("Each trial must include a stimulus.");
        }
        if (request.getResponse() == null || request.getResponse().isBlank()) {
            throw new BadRequestException("Each trial must include a response.");
        }
        if (request.getReactionTime() == null || request.getReactionTime() < 0) {
            throw new BadRequestException("Each trial must include a valid reaction time.");
        }

        LocalDateTime timestamp = request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now();

        return new TrialData(
            session,
            request.getStimulus(),
            request.getResponse(),
            request.getReactionTime(),
            request.isCorrect(),
            timestamp
        );
    }

    private AggregatedMetrics buildMetrics(TestSession session, List<TrialData> trials) {
        double avgReactionTime = trials.stream()
            .mapToLong(TrialData::getReactionTime)
            .average()
            .orElse(0);

        long correctCount = trials.stream()
            .filter(TrialData::isCorrect)
            .count();

        double accuracy = trials.isEmpty() ? 0 : (correctCount * 100.0) / trials.size();
        double errorRate = 100.0 - accuracy;

        return new AggregatedMetrics(
            session,
            round(avgReactionTime),
            round(accuracy),
            round(errorRate)
        );
    }

    private SessionResultResponse toResponse(TestSession session, AggregatedMetrics metrics, List<TrialData> trials) {
        List<TrialResultResponse> trialResponses = trials.stream()
            .map(trial -> new TrialResultResponse(
                trial.getId(),
                trial.getStimulus(),
                trial.getResponse(),
                trial.isCorrect(),
                trial.getReactionTime()
            ))
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
            trialResponses
        );
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String resolveTaskTitle(String taskType) {
        try {
            return taskCatalogService.getTaskById(taskType).getTitle();
        } catch (BadRequestException ex) {
            return taskType;
        }
    }
}
