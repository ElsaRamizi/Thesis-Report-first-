package com.example.demo.service;

import com.example.demo.dto.SessionCompleteRequest;
import com.example.demo.dto.SessionEndRequest;
import com.example.demo.dto.SessionHistoryItemResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.mapper.SessionResultMapper;
import com.example.demo.dto.SessionStartRequest;
import com.example.demo.dto.SessionStartResponse;
import com.example.demo.dto.TrialDataRequest;
import com.example.demo.dto.TrialResultResponse;
import com.example.demo.dto.TrialSubmitRequest;
import com.example.demo.analytics.CognitiveMetricsCalculator;
import com.example.demo.analytics.CognitiveMetricsCalculator.ExtendedSessionMetrics;
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
// saves sessions, trials, and calculated metrics after each game
public class SessionService {

    private static final String HIT = "HIT";
    private static final String CORRECT_REJECTION = "CORRECT_REJECTION";
    private static final String FALSE_ALARM = "FALSE_ALARM";
    private static final String MISS = "MISS";

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

    /// Dual N-Back — create empty test_sessions row, return id for trial posts
    @Transactional
    public SessionStartResponse startSession(String email, SessionStartRequest request) {
        if (request == null) {
            throw new BadRequestException("Missing body.");
        }
        if (request.getTaskType() == null || request.getTaskType().isBlank()) {
            throw new BadRequestException("Task type missing.");
        }
        if (request.getDifficultyLevel() == null || request.getDifficultyLevel().isBlank()) {
            throw new BadRequestException("Difficulty missing.");
        }

        User user = findAuthenticatedUser(email);
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now();
        Integer initialN = request.getInitialN() != null ? request.getInitialN() : 2;

        TestSession session = new TestSession(
            user,
            request.getTaskType(),
            startTime,
            null,
            request.getDifficultyLevel(),
            initialN,
            initialN
        );
        TestSession savedSession = testSessionRepository.save(session);
        return new SessionStartResponse(savedSession.getId(), savedSession.getTaskType(), savedSession.getInitialN());
    }

    /// save one trial row while N-Back is running
    @Transactional
    public TrialResultResponse recordTrial(String email, TrialSubmitRequest request) {
        if (request == null || request.getSessionId() == null) {
            throw new BadRequestException("Session id missing.");
        }

        TestSession session = findOwnedSession(email, request.getSessionId());
        TrialData savedTrial = trialDataRepository.save(toTrialData(session, request));
        return toTrialResponse(savedTrial);
    }

    /// N-Back finished — set end time, build aggregated_metrics from all trials
    @Transactional
    public SessionResultResponse endSession(String email, SessionEndRequest request) {
        if (request == null || request.getSessionId() == null) {
            throw new BadRequestException("Session id missing.");
        }

        TestSession session = findOwnedSession(email, request.getSessionId());
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now();
        if (endTime.isBefore(session.getStartTime())) {
            throw new BadRequestException("End time before start.");
        }

        session.setEndTime(endTime);
        if (request.getFinalN() != null) {
            session.setFinalN(request.getFinalN());
        }
        TestSession savedSession = testSessionRepository.save(session);

        List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(savedSession);
        if (trials.isEmpty()) {
            throw new BadRequestException("Need at least one trial.");
        }

        AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(savedSession)
            .map(existing -> updateMetrics(existing, trials))
            .orElseGet(() -> buildMetrics(savedSession, trials));
        AggregatedMetrics savedMetrics = aggregatedMetricsRepository.save(metrics);

        return toResponse(savedSession, savedMetrics, trials);
    }

    /// Stroop / Memory Span — whole session + trials in one request from frontend
    @Transactional
    public SessionResultResponse completeSession(String email, SessionCompleteRequest request) {
        validateRequest(request);

        User user = findAuthenticatedUser(email);

        TestSession session = new TestSession(
            user,
            request.getTaskType(),
            request.getStartTime(),
            request.getEndTime(),
            request.getDifficultyLevel(),
            request.getInitialN(),
            request.getFinalN()
        );
        TestSession savedSession = testSessionRepository.save(session);

        List<TrialData> savedTrials = new ArrayList<>();
        int trialIndex = 1;
        for (TrialDataRequest trialRequest : request.getTrials()) {
            if (trialRequest.getTrialIndex() == null) {
                trialRequest.setTrialIndex(trialIndex);
            }
            savedTrials.add(trialDataRepository.save(toTrialData(savedSession, trialRequest)));
            trialIndex++;
        }

        AggregatedMetrics metrics = aggregatedMetricsRepository.save(buildMetrics(savedSession, savedTrials));

        return toResponse(savedSession, metrics, savedTrials);
    }

    /// old sessions missing metrics row — rebuild from trial_data (used by compare)
    @Transactional
    public AggregatedMetrics ensureMetrics(TestSession session) {
        return aggregatedMetricsRepository.findBySessionId(session)
            .orElseGet(() -> {
                List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);
                if (trials.isEmpty()) {
                    throw new BadRequestException("No trials saved for this session.");
                }
                return aggregatedMetricsRepository.save(buildMetrics(session, trials));
            });
    }

    /// most recent session for dashboard / results page
    @Transactional(readOnly = true)
    public SessionResultResponse getLatestSessionResult(String email) {
        User user = findAuthenticatedUser(email);

        TestSession session = testSessionRepository.findTopByUserIdOrderByStartTimeDesc(user)
            .orElseThrow(() -> new BadRequestException("No sessions yet."));

        AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(session)
            .orElseThrow(() -> new BadRequestException("No metrics saved."));

        List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);
        return toResponse(session, metrics, trials);
    }

    /// one session by id — must belong to logged-in user
    @Transactional(readOnly = true)
    public SessionResultResponse getSessionMetrics(String email, Long sessionId) {
        TestSession session = findOwnedSession(email, sessionId);
        AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(session)
            .orElseThrow(() -> new BadRequestException("No metrics for session."));
        List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);
        return toResponse(session, metrics, trials);
    }

    /// list of past sessions with summary metrics for history page
    @Transactional(readOnly = true)
    public List<SessionHistoryItemResponse> getSessionHistory(String email) {
        User user = findAuthenticatedUser(email);

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
            throw new BadRequestException("Missing body.");
        }
        if (request.getTaskType() == null || request.getTaskType().isBlank()) {
            throw new BadRequestException("Task type missing.");
        }
        if (request.getDifficultyLevel() == null || request.getDifficultyLevel().isBlank()) {
            throw new BadRequestException("Difficulty missing.");
        }
        if (request.getStartTime() == null) {
            throw new BadRequestException("Start time missing.");
        }
        if (request.getEndTime() == null) {
            throw new BadRequestException("End time missing.");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time before start.");
        }
        if (request.getTrials() == null || request.getTrials().isEmpty()) {
            throw new BadRequestException("Need at least one trial.");
        }
    }

    private TrialData toTrialData(TestSession session, TrialDataRequest request) {
        if (isDualNBackTrial(request)) {
            return toDualNBackTrialData(session, request);
        }

        if (request.getStimulus() == null || request.getStimulus().isBlank()) {
            throw new BadRequestException("Trial needs stimulus.");
        }
        if (request.getResponse() == null || request.getResponse().isBlank()) {
            throw new BadRequestException("Trial needs response.");
        }
        if (request.getReactionTime() == null || request.getReactionTime() < 0) {
            throw new BadRequestException("Invalid reaction time.");
        }

        LocalDateTime timestamp = request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now();

        TrialData trialData = new TrialData(
            session,
            request.getStimulus(),
            request.getResponse(),
            request.getReactionTime(),
            request.isCorrect(),
            timestamp
        );
        if (request.getTrialIndex() != null) {
            trialData.setTrialIndex(request.getTrialIndex());
        }
        return trialData;
    }

    private TrialData toDualNBackTrialData(TestSession session, TrialDataRequest request) {
        if (request.getTrialIndex() == null || request.getTrialIndex() < 1) {
            throw new BadRequestException("Trial index missing.");
        }
        if (request.getNLevel() == null || request.getNLevel() < 1) {
            throw new BadRequestException("N level missing.");
        }
        if (request.getPosition() == null || request.getPosition() < 0 || request.getPosition() > 8) {
            throw new BadRequestException("Position must be 0-8.");
        }
        if (request.getLetter() == null || request.getLetter().isBlank()) {
            throw new BadRequestException("Letter missing.");
        }

        boolean positionCorrect = isCorrectOutcome(request.getPositionOutcome());
        boolean letterCorrect = isCorrectOutcome(request.getLetterOutcome());
        Long averageReactionTime = averageNullable(request.getReactionTimePosition(), request.getReactionTimeLetter());
        String stimulus = "position:" + request.getPosition() + ",letter:" + request.getLetter();
        String response = "position:" + Boolean.TRUE.equals(request.getUserPressedPosition())
            + ",letter:" + Boolean.TRUE.equals(request.getUserPressedLetter());
        LocalDateTime timestamp = request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now();

        return new TrialData(
            session,
            stimulus,
            response,
            averageReactionTime,
            positionCorrect && letterCorrect,
            timestamp,
            request.getTrialIndex(),
            request.getNLevel(),
            request.getPosition(),
            request.getLetter(),
            Boolean.TRUE.equals(request.getExpectedPositionMatch()),
            Boolean.TRUE.equals(request.getExpectedLetterMatch()),
            Boolean.TRUE.equals(request.getUserPressedPosition()),
            Boolean.TRUE.equals(request.getUserPressedLetter()),
            request.getPositionOutcome(),
            request.getLetterOutcome(),
            request.getReactionTimePosition(),
            request.getReactionTimeLetter(),
            request.getStimulusStartTime()
        );
    }

    /// compute accuracy, avg RT, stroop/span/n-back specific fields from trials
    private AggregatedMetrics buildMetrics(TestSession session, List<TrialData> trials) {
        if (trials.stream().anyMatch(trial -> trial.getPositionOutcome() != null || trial.getLetterOutcome() != null)) {
            return buildDualNBackMetrics(session, trials);
        }

        double avgReactionTime = trials.stream()
            .mapToLong(TrialData::getReactionTime)
            .average()
            .orElse(0);

        long correctCount = trials.stream()
            .filter(TrialData::isCorrect)
            .count();

        double accuracy = trials.isEmpty() ? 0 : (correctCount * 100.0) / trials.size();
        double errorRate = 100.0 - accuracy;

        AggregatedMetrics metrics = new AggregatedMetrics(
            session,
            round(avgReactionTime),
            round(accuracy),
            round(errorRate)
        );
        applyExtendedMetrics(metrics, session, trials);
        applyTaskSpecificMetrics(metrics, session, trials);
        return metrics;
    }

    private AggregatedMetrics buildDualNBackMetrics(TestSession session, List<TrialData> trials) {
        long correctOutcomes = 0;
        long totalOutcomes = 0;
        long falseAlarms = 0;
        long nonMatchOpportunities = 0;
        long hits = 0;
        long matchOpportunities = 0;
        long reactionCount = 0;
        long reactionTotal = 0;
        int maxNReached = session.getInitialN() != null ? session.getInitialN() : 1;

        for (TrialData trial : trials) {
            maxNReached = Math.max(maxNReached, trial.getNLevel() != null ? trial.getNLevel() : maxNReached);
            correctOutcomes += isCorrectOutcome(trial.getPositionOutcome()) ? 1 : 0;
            correctOutcomes += isCorrectOutcome(trial.getLetterOutcome()) ? 1 : 0;
            totalOutcomes += 2;

            if (Boolean.TRUE.equals(trial.getExpectedPositionMatch())) {
                matchOpportunities++;
                hits += HIT.equals(trial.getPositionOutcome()) ? 1 : 0;
            } else {
                nonMatchOpportunities++;
                falseAlarms += FALSE_ALARM.equals(trial.getPositionOutcome()) ? 1 : 0;
            }

            if (Boolean.TRUE.equals(trial.getExpectedLetterMatch())) {
                matchOpportunities++;
                hits += HIT.equals(trial.getLetterOutcome()) ? 1 : 0;
            } else {
                nonMatchOpportunities++;
                falseAlarms += FALSE_ALARM.equals(trial.getLetterOutcome()) ? 1 : 0;
            }

            if (trial.getReactionTimePosition() != null) {
                reactionTotal += trial.getReactionTimePosition();
                reactionCount++;
            }
            if (trial.getReactionTimeLetter() != null) {
                reactionTotal += trial.getReactionTimeLetter();
                reactionCount++;
            }
        }

        double accuracy = totalOutcomes == 0 ? 0 : (correctOutcomes * 100.0) / totalOutcomes;
        double falseAlarmRate = nonMatchOpportunities == 0 ? 0 : falseAlarms / (double) nonMatchOpportunities;
        double hitRate = matchOpportunities == 0 ? 0 : hits / (double) matchOpportunities;
        double avgReactionTime = reactionCount == 0 ? 0 : reactionTotal / (double) reactionCount;

        AggregatedMetrics metrics = new AggregatedMetrics(
            session,
            round(avgReactionTime),
            round(accuracy),
            round(100.0 - accuracy),
            round(falseAlarmRate),
            maxNReached,
            round(calculateDPrime(hitRate, falseAlarmRate, matchOpportunities, nonMatchOpportunities))
        );
        applyExtendedMetrics(metrics, session, trials);
        applyTaskSpecificMetrics(metrics, session, trials);
        return metrics;
    }

    private void applyExtendedMetrics(AggregatedMetrics metrics, TestSession session, List<TrialData> trials) {
        ExtendedSessionMetrics extended = CognitiveMetricsCalculator.computeExtendedMetrics(session, trials);
        metrics.setMedianReactionTime(extended.medianReactionTime());
        metrics.setMissRate(extended.missRate());
        metrics.setResponseVariability(extended.responseVariability());
    }

    private AggregatedMetrics updateMetrics(AggregatedMetrics existing, List<TrialData> trials) {
        AggregatedMetrics fresh = buildMetrics(existing.getSessionId(), trials);
        existing.setAvgReactionTime(fresh.getAvgReactionTime());
        existing.setAccuracy(fresh.getAccuracy());
        existing.setErrorRate(fresh.getErrorRate());
        existing.setFalseAlarmRate(fresh.getFalseAlarmRate());
        existing.setMaxNReached(fresh.getMaxNReached());
        existing.setDPrime(fresh.getDPrime());
        existing.setMedianReactionTime(fresh.getMedianReactionTime());
        existing.setMissRate(fresh.getMissRate());
        existing.setResponseVariability(fresh.getResponseVariability());
        existing.setMaxSpanReached(fresh.getMaxSpanReached());
        existing.setStroopInterferenceMs(fresh.getStroopInterferenceMs());
        existing.setStroopCongruentAccuracy(fresh.getStroopCongruentAccuracy());
        existing.setStroopIncongruentAccuracy(fresh.getStroopIncongruentAccuracy());
        return existing;
    }

    private void applyTaskSpecificMetrics(AggregatedMetrics metrics, TestSession session, List<TrialData> trials) {
        if (trials.stream().anyMatch(trial -> trial.getStimulus() != null && trial.getStimulus().contains("|congruent"))) {
            applyStroopMetrics(metrics, trials);
        }
        if ("memory-span".equalsIgnoreCase(session.getTaskType())
            || trials.stream().anyMatch(trial -> trial.getStimulus() != null && trial.getStimulus().matches("^[0-9-]+$"))) {
            applyMemorySpanMetrics(metrics, trials);
        }
    }

    private void applyStroopMetrics(AggregatedMetrics metrics, List<TrialData> trials) {
        List<TrialData> congruentTrials = trials.stream()
            .filter(trial -> trial.getStimulus() != null && trial.getStimulus().endsWith("|congruent"))
            .toList();
        List<TrialData> incongruentTrials = trials.stream()
            .filter(trial -> trial.getStimulus() != null && trial.getStimulus().endsWith("|incongruent"))
            .toList();

        double congruentRt = congruentTrials.stream().mapToLong(TrialData::getReactionTime).average().orElse(0);
        double incongruentRt = incongruentTrials.stream().mapToLong(TrialData::getReactionTime).average().orElse(0);
        metrics.setStroopInterferenceMs(round(incongruentRt - congruentRt));
        metrics.setStroopCongruentAccuracy(round(trialAccuracyPercent(congruentTrials)));
        metrics.setStroopIncongruentAccuracy(round(trialAccuracyPercent(incongruentTrials)));
    }

    private void applyMemorySpanMetrics(AggregatedMetrics metrics, List<TrialData> trials) {
        int maxSpan = trials.stream()
            .mapToInt(trial -> trial.getStimulus() == null ? 0 : trial.getStimulus().split("-").length)
            .max()
            .orElse(0);
        metrics.setMaxSpanReached(maxSpan);
    }

    private double trialAccuracyPercent(List<TrialData> trials) {
        if (trials.isEmpty()) {
            return 0;
        }
        long correct = trials.stream().filter(TrialData::isCorrect).count();
        return (correct * 100.0) / trials.size();
    }

    private SessionResultResponse toResponse(TestSession session, AggregatedMetrics metrics, List<TrialData> trials) {
        return SessionResultMapper.toResponse(
            session,
            metrics,
            trials,
            this::resolveTaskTitle,
            this::toTrialResponse
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

    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found."));
    }

    private TestSession findOwnedSession(String email, Long sessionId) {
        User user = findAuthenticatedUser(email);
        TestSession session = testSessionRepository.findById(sessionId)
            .orElseThrow(() -> new BadRequestException("Session not found."));

        if (!session.getUserId().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not your session.");
        }

        return session;
    }

    private boolean isDualNBackTrial(TrialDataRequest request) {
        return request.getNLevel() != null
            || request.getPositionOutcome() != null
            || request.getLetterOutcome() != null
            || request.getPosition() != null
            || request.getLetter() != null;
    }

    private boolean isCorrectOutcome(String outcome) {
        return HIT.equals(outcome) || CORRECT_REJECTION.equals(outcome);
    }

    private Long averageNullable(Long first, Long second) {
        if (first == null && second == null) {
            return 0L;
        }
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return Math.round((first + second) / 2.0);
    }

    private double calculateDPrime(double hitRate, double falseAlarmRate, long hitsTotal, long falseAlarmTotal) {
        double adjustedHitRate = adjustRate(hitRate, hitsTotal);
        double adjustedFalseAlarmRate = adjustRate(falseAlarmRate, falseAlarmTotal);
        return inverseNormalCdf(adjustedHitRate) - inverseNormalCdf(adjustedFalseAlarmRate);
    }

    private double adjustRate(double rate, long total) {
        if (total <= 0) {
            return 0.5;
        }
        double floor = 0.5 / total;
        double ceiling = 1.0 - floor;
        return Math.max(floor, Math.min(ceiling, rate));
    }

    // used for d-prime calculation
    private double inverseNormalCdf(double probability) {
        double[] a = {-39.6968302866538, 220.946098424521, -275.928510446969, 138.357751867269, -30.6647980661472, 2.50662827745924};
        double[] b = {-54.4760987982241, 161.585836858041, -155.698979859887, 66.8013118877197, -13.2806815528857};
        double[] c = {-0.00778489400243029, -0.322396458041136, -2.40075827716184, -2.54973253934373, 4.37466414146497, 2.93816398269878};
        double[] d = {0.00778469570904146, 0.32246712907004, 2.445134137143, 3.75440866190742};

        if (probability <= 0 || probability >= 1) {
            throw new IllegalArgumentException("Probability must be inside (0, 1).");
        }

        double plow = 0.02425;
        double phigh = 1 - plow;

        if (probability < plow) {
            double q = Math.sqrt(-2 * Math.log(probability));
            return (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5])
                / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
        }

        if (probability > phigh) {
            double q = Math.sqrt(-2 * Math.log(1 - probability));
            return -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5])
                / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
        }

        double q = probability - 0.5;
        double r = q * q;
        return (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q
            / (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1);
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
