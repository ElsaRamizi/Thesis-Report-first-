package com.example.demo.service;

import com.example.demo.analytics.CognitiveMetricsCalculator;
import com.example.demo.analytics.CognitiveMetricsCalculator.CognitiveMetricSnapshot;
import com.example.demo.analytics.CognitiveMetricsCalculator.MetricComparison;
import com.example.demo.dto.AutomatedReportResponse;
import com.example.demo.dto.CognitiveExportRequest;
import com.example.demo.dto.GroupParticipantTrendRow;
import com.example.demo.dto.GroupTimelinePointDto;
import com.example.demo.dto.GroupTrendsResponse;
import com.example.demo.dto.ClinicianParticipantResponse;
import com.example.demo.dto.ClinicianSessionSummaryResponse;
import com.example.demo.dto.MetricComparisonDto;
import com.example.demo.dto.MultiSessionCompareResponse;
import com.example.demo.dto.SessionCompareResponse;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.dto.SessionTimelinePointDto;
import com.example.demo.dto.TrialResultResponse;
import com.example.demo.mapper.SessionResultMapper;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import com.example.demo.model.User;
import com.example.demo.repository.AggregatedMetricsRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.TrialDataRepository;
import com.example.demo.repository.ParticipantProfileRepository;
import com.example.demo.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicianService {

    private final UserRepository userRepository;
    private final TestSessionRepository testSessionRepository;
    private final TrialDataRepository trialDataRepository;
    private final AggregatedMetricsRepository aggregatedMetricsRepository;
    private final TaskCatalogService taskCatalogService;
    private final ClinicianAccessService clinicianAccessService;
    private final ParticipantProfileRepository participantProfileRepository;
    private final SessionService sessionService;

    public ClinicianService(
        UserRepository userRepository,
        TestSessionRepository testSessionRepository,
        TrialDataRepository trialDataRepository,
        AggregatedMetricsRepository aggregatedMetricsRepository,
        TaskCatalogService taskCatalogService,
        ClinicianAccessService clinicianAccessService,
        ParticipantProfileRepository participantProfileRepository,
        SessionService sessionService
    ) {
        this.userRepository = userRepository;
        this.testSessionRepository = testSessionRepository;
        this.trialDataRepository = trialDataRepository;
        this.aggregatedMetricsRepository = aggregatedMetricsRepository;
        this.taskCatalogService = taskCatalogService;
        this.clinicianAccessService = clinicianAccessService;
        this.participantProfileRepository = participantProfileRepository;
        this.sessionService = sessionService;
    }

    @Transactional(readOnly = true)
    public List<ClinicianParticipantResponse> getParticipants(String clinicianEmail) {
        return clinicianAccessService.getAccessibleParticipants(clinicianEmail).stream()
            .map(this::toParticipantResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicianSessionSummaryResponse> getParticipantSessions(String clinicianEmail, Long participantId) {
        User participant = clinicianAccessService.requireAccessibleParticipant(clinicianEmail, participantId);
        return testSessionRepository.findByUserIdOrderByStartTimeDesc(participant).stream()
            .map(this::toSessionSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public SessionResultResponse getSessionResult(String clinicianEmail, Long sessionId) {
        TestSession session = findAccessibleParticipantSession(clinicianEmail, sessionId);
        AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(session)
            .orElseThrow(() -> new BadRequestException("Metrics were not found for this session."));
        List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);

        return toResultResponse(session, metrics, trials);
    }

    @Transactional
    public SessionCompareResponse compareSessions(String clinicianEmail, Long sessionAId, Long sessionBId) {
        TestSession sessionA = findAccessibleParticipantSession(clinicianEmail, sessionAId);
        TestSession sessionB = findAccessibleParticipantSession(clinicianEmail, sessionBId);

        if (!sessionA.getUserId().getId().equals(sessionB.getUserId().getId())) {
            throw new BadRequestException("Both sessions must be from the same person.");
        }

        AggregatedMetrics metricsA = sessionService.ensureMetrics(sessionA);
        AggregatedMetrics metricsB = sessionService.ensureMetrics(sessionB);

        TestSession earlier = sessionA.getStartTime().isBefore(sessionB.getStartTime()) ? sessionA : sessionB;
        TestSession later = earlier.getId().equals(sessionA.getId()) ? sessionB : sessionA;
        AggregatedMetrics earlierMetrics = earlier.getId().equals(sessionA.getId()) ? metricsA : metricsB;
        AggregatedMetrics laterMetrics = later.getId().equals(sessionB.getId()) ? metricsB : metricsA;

        List<TrialData> trialsA = trialDataRepository.findBySessionIdOrderByTimestampAsc(sessionA);
        List<TrialData> trialsB = trialDataRepository.findBySessionIdOrderByTimestampAsc(sessionB);

        CognitiveMetricSnapshot baseline = CognitiveMetricsCalculator.snapshotFromMetrics(earlierMetrics);
        CognitiveMetricSnapshot current = CognitiveMetricsCalculator.snapshotFromMetrics(laterMetrics);
        MetricComparison comparison = CognitiveMetricsCalculator.compareToBaseline(current, baseline);

        return new SessionCompareResponse(
            toResultResponse(sessionA, metricsA, trialsA),
            toResultResponse(sessionB, metricsB, trialsB),
            toComparisonDto(comparison, earlier, later)
        );
    }

    @Transactional
    public MultiSessionCompareResponse compareMultipleSessions(String clinicianEmail, List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.size() < 2) {
            throw new BadRequestException("Select at least two sessions to compare.");
        }
        if (sessionIds.size() > 8) {
            throw new BadRequestException("You can compare up to eight sessions at once.");
        }

        List<TestSession> sessions = new ArrayList<>();
        for (Long sessionId : sessionIds) {
            sessions.add(findAccessibleParticipantSession(clinicianEmail, sessionId));
        }

        Long participantId = sessions.get(0).getUserId().getId();
        boolean sameParticipant = sessions.stream().allMatch(session -> session.getUserId().getId().equals(participantId));
        if (!sameParticipant) {
            throw new BadRequestException("All selected sessions must belong to the same participant.");
        }

        sessions.sort(Comparator.comparing(TestSession::getStartTime));
        List<SessionResultResponse> sessionResults = new ArrayList<>();
        List<SessionTimelinePointDto> timeline = new ArrayList<>();

        for (TestSession session : sessions) {
            AggregatedMetrics metrics = sessionService.ensureMetrics(session);
            List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);
            sessionResults.add(toResultResponse(session, metrics, trials));
            timeline.add(new SessionTimelinePointDto(
                session.getId(),
                session.getTaskType(),
                resolveTaskTitle(session.getTaskType()),
                session.getStartTime(),
                metrics.getAccuracy(),
                metrics.getAvgReactionTime()
            ));
        }

        TestSession earliest = sessions.get(0);
        TestSession latest = sessions.get(sessions.size() - 1);
        AggregatedMetrics earliestMetrics = sessionService.ensureMetrics(earliest);
        AggregatedMetrics latestMetrics = sessionService.ensureMetrics(latest);
        MetricComparison comparison = CognitiveMetricsCalculator.compareToBaseline(
            CognitiveMetricsCalculator.snapshotFromMetrics(latestMetrics),
            CognitiveMetricsCalculator.snapshotFromMetrics(earliestMetrics)
        );

        return new MultiSessionCompareResponse(
            sessionResults,
            timeline,
            toComparisonDto(comparison, earliest, latest),
            String.format(
                "Compared %d sessions from %s to %s.",
                sessions.size(),
                earliest.getStartTime(),
                latest.getStartTime()
            )
        );
    }

    @Transactional(readOnly = true)
    public AutomatedReportResponse generateAutomatedReport(String clinicianEmail, Long participantId) {
        User participant = clinicianAccessService.requireAccessibleParticipant(clinicianEmail, participantId);
        List<TestSession> sessions = testSessionRepository.findByUserIdOrderByStartTimeDesc(participant).stream()
            .sorted(Comparator.comparing(TestSession::getStartTime))
            .toList();

        List<String> findings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (sessions.isEmpty()) {
            findings.add("No sessions saved for this person yet.");
            recommendations.add("They should play at least one task first.");
            return new AutomatedReportResponse(
                participant.getId(),
                participant.getEmail(),
                0,
                findings,
                recommendations,
                "INSUFFICIENT_DATA",
                "NORMAL"
            );
        }

        List<CognitiveMetricSnapshot> snapshots = sessions.stream()
            .map(session -> aggregatedMetricsRepository.findBySessionId(session).orElse(null))
            .filter(metrics -> metrics != null)
            .map(CognitiveMetricsCalculator::snapshotFromMetrics)
            .toList();

        if (snapshots.size() < 2) {
            findings.add("Only one session so far — need at least 2 for a trend.");
            recommendations.add("Do another session in a week or two, then check again.");
            return new AutomatedReportResponse(
                participant.getId(),
                participant.getEmail(),
                snapshots.size(),
                findings,
                recommendations,
                "INSUFFICIENT_DATA",
                "NORMAL"
            );
        }

        String severity = "NORMAL";

        CognitiveMetricSnapshot first = snapshots.get(0);
        CognitiveMetricSnapshot latest = snapshots.get(snapshots.size() - 1);
        MetricComparison overall = CognitiveMetricsCalculator.compareToBaseline(latest, first);

        appendMetricFinding(findings, "Overall accuracy", overall.accuracyDeltaPercent(), false);
        appendMetricFinding(findings, "Overall reaction time", overall.reactionTimeDeltaPercent(), true);
        appendMetricFinding(findings, "Overall error rate", overall.errorRateDeltaPercent(), true);

        if (snapshots.size() >= 3) {
            List<CognitiveMetricSnapshot> recentSnapshots = snapshots.subList(snapshots.size() - 3, snapshots.size());
            MetricComparison recent = CognitiveMetricsCalculator.compareToBaseline(
                recentSnapshots.get(recentSnapshots.size() - 1),
                recentSnapshots.get(0)
            );
            severity = mergeSeverity(severity, evaluateRecentSeverity(recent, findings));

            AggregatedMetrics latestMetrics = aggregatedMetricsRepository.findBySessionId(sessions.get(sessions.size() - 1)).orElse(null);
            if (latestMetrics != null) {
                if (latestMetrics.getMissRate() != null && latestMetrics.getMissRate() >= 0.2) {
                    findings.add(String.format(
                        "Latest session miss rate is high: %.1f%%.",
                        latestMetrics.getMissRate() * 100
                    ));
                    severity = mergeSeverity(severity, "ALERT");
                }
                if (latestMetrics.getFalseAlarmRate() != null && latestMetrics.getFalseAlarmRate() >= 0.25) {
                    findings.add(String.format(
                        "Latest session false alarms a bit high: %.1f%%.",
                        latestMetrics.getFalseAlarmRate() * 100
                    ));
                    severity = mergeSeverity(severity, "WATCH");
                }
            }
        }

        if (overall.accuracyDeltaPercent() != null && overall.accuracyDeltaPercent() >= 10) {
            findings.add(String.format("Accuracy up about %.1f%% since first session.", overall.accuracyDeltaPercent()));
        }
        if (overall.reactionTimeDeltaPercent() != null && overall.reactionTimeDeltaPercent() >= 15) {
            findings.add(String.format("Reaction time better by about %.1f%% since first session.", overall.reactionTimeDeltaPercent()));
        }

        Double improvementRate = CognitiveMetricsCalculator.computeImprovementRate(snapshots);
        if (improvementRate != null && improvementRate >= 5) {
            findings.add(String.format("Overall scores improved about %.1f%% across sessions.", improvementRate));
        } else if (improvementRate != null && improvementRate <= -5) {
            findings.add(String.format("Overall scores down about %.1f%% across sessions.", Math.abs(improvementRate)));
        }

        if (overall.accuracyDeltaPercent() != null && overall.accuracyDeltaPercent() <= -8) {
            recommendations.add("Accuracy dropped — maybe run Stroop or Memory Span again.");
        }
        if (overall.reactionTimeDeltaPercent() != null && overall.reactionTimeDeltaPercent() <= -10) {
            recommendations.add("RT got slower — could be tired or distracted, worth asking.");
        }
        if (overall.accuracyDeltaPercent() != null && overall.accuracyDeltaPercent() >= 8) {
            recommendations.add("Accuracy improved — keep playing regularly if that works for them.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Looks stable for now — check again after a few more sessions.");
        }

        String overallTrend = resolveOverallTrend(overall);
        return new AutomatedReportResponse(
            participant.getId(),
            participant.getEmail(),
            snapshots.size(),
            findings,
            recommendations,
            overallTrend,
            severity
        );
    }

    @Transactional(readOnly = true)
    public GroupTrendsResponse getGroupTrends(String clinicianEmail, String taskFilter) {
        List<User> participants = clinicianAccessService.getAccessibleParticipants(clinicianEmail);
        List<GroupParticipantTrendRow> rows = new ArrayList<>();
        Map<String, List<AggregatedMetrics>> timelineBuckets = new HashMap<>();

        for (User participant : participants) {
            List<TestSession> sessions = testSessionRepository.findByUserIdOrderByStartTimeDesc(participant).stream()
                .filter(session -> matchesTaskFilter(session, taskFilter))
                .sorted(Comparator.comparing(TestSession::getStartTime))
                .toList();

            if (sessions.isEmpty()) {
                continue;
            }

            TestSession latestSession = sessions.get(sessions.size() - 1);
            AggregatedMetrics latestMetrics = aggregatedMetricsRepository.findBySessionId(latestSession).orElse(null);
            if (latestMetrics == null) {
                continue;
            }

            List<CognitiveMetricSnapshot> snapshots = sessions.stream()
                .map(session -> aggregatedMetricsRepository.findBySessionId(session).orElse(null))
                .filter(metrics -> metrics != null)
                .map(CognitiveMetricsCalculator::snapshotFromMetrics)
                .toList();

            String trend = "STABLE";
            String severity = "NORMAL";
            if (snapshots.size() >= 2) {
                MetricComparison comparison = CognitiveMetricsCalculator.compareToBaseline(
                    snapshots.get(snapshots.size() - 1),
                    snapshots.get(0)
                );
                trend = resolveOverallTrend(comparison);
                if (snapshots.size() >= 3) {
                    severity = evaluateRecentSeverity(
                        CognitiveMetricsCalculator.compareToBaseline(
                            snapshots.subList(snapshots.size() - 3, snapshots.size())
                                .get(snapshots.subList(snapshots.size() - 3, snapshots.size()).size() - 1),
                            snapshots.subList(snapshots.size() - 3, snapshots.size()).get(0)
                        ),
                        new ArrayList<>()
                    );
                }
            }

            rows.add(new GroupParticipantTrendRow(
                participant.getId(),
                participant.getEmail(),
                latestSession.getTaskType(),
                resolveTaskTitle(latestSession.getTaskType()),
                latestSession.getStartTime(),
                latestMetrics.getAccuracy(),
                latestMetrics.getAvgReactionTime(),
                trend,
                severity
            ));

            for (TestSession session : sessions) {
                aggregatedMetricsRepository.findBySessionId(session).ifPresent(metrics -> {
                    String bucket = session.getStartTime().toLocalDate().toString();
                    timelineBuckets.computeIfAbsent(bucket, key -> new ArrayList<>()).add(metrics);
                });
            }
        }

        List<GroupTimelinePointDto> timeline = timelineBuckets.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                List<AggregatedMetrics> metrics = entry.getValue();
                double avgAccuracy = metrics.stream().mapToDouble(AggregatedMetrics::getAccuracy).average().orElse(0);
                double avgRt = metrics.stream().mapToDouble(AggregatedMetrics::getAvgReactionTime).average().orElse(0);
                return new GroupTimelinePointDto(
                    entry.getKey(),
                    LocalDate.parse(entry.getKey()).atStartOfDay(),
                    round(avgAccuracy),
                    round(avgRt),
                    metrics.size()
                );
            })
            .toList();

        return new GroupTrendsResponse(taskFilter, rows, timeline);
    }

    @Transactional(readOnly = true)
    public String exportAnonymizedCognitiveMetrics(String clinicianEmail, CognitiveExportRequest request) {
        CognitiveExportRequest filters = request == null ? new CognitiveExportRequest(null, null, null, false) : request;
        User clinician = clinicianAccessService.findClinician(clinicianEmail);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        StringBuilder csv = new StringBuilder(
            "anon_participant_id,session_id,task_type,difficulty_level,start_time,end_time,"
                + "avg_reaction_time_ms,median_reaction_time_ms,accuracy_pct,error_rate_pct,"
                + "false_alarm_rate,miss_rate,max_n_reached,d_prime,response_variability\n"
        );

        for (User participant : clinicianAccessService.getAccessibleParticipants(clinicianEmail)) {
            if (Boolean.TRUE.equals(filters.assignedOnly())
                && participantProfileRepository.findByUser(participant)
                    .map(profile -> profile.getAssignedClinician())
                    .map(assigned -> !assigned.getId().equals(clinician.getId()))
                    .orElse(true)) {
                continue;
            }

            String anonId = anonymizeParticipantId(participant.getId());
            for (TestSession session : testSessionRepository.findByUserIdOrderByStartTimeDesc(participant)) {
                if (!matchesTaskFilter(session, filters.taskType())) {
                    continue;
                }
                if (filters.startDate() != null && session.getStartTime().toLocalDate().isBefore(filters.startDate())) {
                    continue;
                }
                if (filters.endDate() != null && session.getStartTime().toLocalDate().isAfter(filters.endDate())) {
                    continue;
                }

                AggregatedMetrics metrics = aggregatedMetricsRepository.findBySessionId(session).orElse(null);
                if (metrics == null) {
                    continue;
                }

                csv.append(anonId).append(',')
                    .append(session.getId()).append(',')
                    .append(csvEscape(session.getTaskType())).append(',')
                    .append(csvEscape(session.getDifficultyLevel())).append(',')
                    .append(session.getStartTime() != null ? formatter.format(session.getStartTime()) : "").append(',')
                    .append(session.getEndTime() != null ? formatter.format(session.getEndTime()) : "").append(',')
                    .append(metrics.getAvgReactionTime()).append(',')
                    .append(metrics.getMedianReactionTime() != null ? metrics.getMedianReactionTime() : "").append(',')
                    .append(metrics.getAccuracy()).append(',')
                    .append(metrics.getErrorRate()).append(',')
                    .append(metrics.getFalseAlarmRate() != null ? metrics.getFalseAlarmRate() : "").append(',')
                    .append(metrics.getMissRate() != null ? metrics.getMissRate() : "").append(',')
                    .append(metrics.getMaxNReached() != null ? metrics.getMaxNReached() : "").append(',')
                    .append(metrics.getDPrime() != null ? metrics.getDPrime() : "").append(',')
                    .append(metrics.getResponseVariability() != null ? metrics.getResponseVariability() : "")
                    .append('\n');
            }
        }

        return csv.toString();
    }

    @Transactional(readOnly = true)
    public List<ClinicianParticipantResponse> getClinicianDirectory() {
        return userRepository.findByRoleOrderByCreatedAtDesc("CLINICIAN").stream()
            .map(clinician -> new ClinicianParticipantResponse(
                clinician.getId(),
                clinician.getEmail(),
                0,
                null,
                null,
                null
            ))
            .toList();
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

    private TestSession findAccessibleParticipantSession(String clinicianEmail, Long sessionId) {
        TestSession session = testSessionRepository.findById(sessionId)
            .orElseThrow(() -> new BadRequestException("Session was not found."));

        if (!"USER".equals(session.getUserId().getRole())) {
            throw new BadRequestException("Only participant sessions can be viewed here.");
        }

        clinicianAccessService.requireAccessibleParticipant(clinicianEmail, session.getUserId().getId());
        return session;
    }

    private String evaluateRecentSeverity(MetricComparison recent, List<String> findings) {
        String severity = "NORMAL";
        if (recent.accuracyDeltaPercent() != null && recent.accuracyDeltaPercent() <= -10) {
            findings.add(String.format(
                "Last 3 sessions: accuracy down about %.1f%%.",
                Math.abs(recent.accuracyDeltaPercent())
            ));
            severity = "ALERT";
        } else if (recent.accuracyDeltaPercent() != null && recent.accuracyDeltaPercent() <= -5) {
            findings.add(String.format(
                "Last 3 sessions: accuracy a bit down (%.1f%%).",
                Math.abs(recent.accuracyDeltaPercent())
            ));
            severity = mergeSeverity(severity, "WATCH");
        }

        if (recent.reactionTimeDeltaPercent() != null && recent.reactionTimeDeltaPercent() <= -15) {
            findings.add(String.format(
                "Last 3 sessions: RT slower by about %.1f%%.",
                Math.abs(recent.reactionTimeDeltaPercent())
            ));
            severity = mergeSeverity(severity, "ALERT");
        } else if (recent.reactionTimeDeltaPercent() != null && recent.reactionTimeDeltaPercent() <= -8) {
            findings.add("Last 3 sessions: responses got slower.");
            severity = mergeSeverity(severity, "WATCH");
        }

        if (recent.errorRateDeltaPercent() != null && recent.errorRateDeltaPercent() >= 10) {
            findings.add(String.format(
                "Last 3 sessions: error rate up about %.1f%%.",
                recent.errorRateDeltaPercent()
            ));
            severity = mergeSeverity(severity, "WATCH");
        }

        return severity;
    }

    private String mergeSeverity(String current, String next) {
        if ("ALERT".equals(current) || "ALERT".equals(next)) {
            return "ALERT";
        }
        if ("WATCH".equals(current) || "WATCH".equals(next)) {
            return "WATCH";
        }
        return "NORMAL";
    }

    private boolean matchesTaskFilter(TestSession session, String taskFilter) {
        if (taskFilter == null || taskFilter.isBlank() || "all".equalsIgnoreCase(taskFilter)) {
            return true;
        }
        return taskFilter.equalsIgnoreCase(session.getTaskType());
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private MetricComparisonDto toComparisonDto(MetricComparison comparison, TestSession earlier, TestSession later) {
        List<String> parts = new ArrayList<>();
        if (comparison.accuracyDeltaPercent() != null) {
            parts.add(formatDelta("Accuracy", comparison.accuracyDeltaPercent(), false));
        }
        if (comparison.reactionTimeDeltaPercent() != null) {
            parts.add(formatDelta("Reaction time", comparison.reactionTimeDeltaPercent(), true));
        }
        if (comparison.errorRateDeltaPercent() != null) {
            parts.add(formatDelta("Error rate", comparison.errorRateDeltaPercent(), true));
        }

        String summary = parts.isEmpty()
            ? "Not enough data to compare these sessions."
            : String.format(
                "%s (%s) → %s (%s): %s.",
                resolveTaskTitle(earlier.getTaskType()),
                earlier.getStartTime(),
                resolveTaskTitle(later.getTaskType()),
                later.getStartTime(),
                String.join("; ", parts)
            );

        return new MetricComparisonDto(
            comparison.reactionTimeDeltaPercent(),
            comparison.accuracyDeltaPercent(),
            comparison.errorRateDeltaPercent(),
            comparison.missRateDeltaPercent(),
            summary
        );
    }

    private String formatDelta(String label, double delta, boolean lowerIsBetter) {
        double magnitude = Math.abs(delta);
        boolean improved = lowerIsBetter ? delta < 0 : delta > 0;
        String direction = improved ? "improved" : (delta == 0 ? "unchanged" : "declined");
        return String.format("%s %s by %.1f%%", label, direction, magnitude);
    }

    private void appendMetricFinding(List<String> findings, String label, Double delta, boolean lowerIsBetter) {
        if (delta == null || Math.abs(delta) < 3) {
            return;
        }

        boolean improved = lowerIsBetter ? delta < 0 : delta > 0;
        String direction = improved ? "improved" : "declined";
        findings.add(String.format("%s %s by %.1f%% (first vs latest session).", label, direction, Math.abs(delta)));
    }

    private String resolveOverallTrend(MetricComparison comparison) {
        int score = 0;
        if (comparison.accuracyDeltaPercent() != null) {
            score += comparison.accuracyDeltaPercent() >= 3 ? 1 : (comparison.accuracyDeltaPercent() <= -3 ? -1 : 0);
        }
        if (comparison.reactionTimeDeltaPercent() != null) {
            score += comparison.reactionTimeDeltaPercent() >= 3 ? 1 : (comparison.reactionTimeDeltaPercent() <= -3 ? -1 : 0);
        }
        if (comparison.errorRateDeltaPercent() != null) {
            score += comparison.errorRateDeltaPercent() <= -3 ? 1 : (comparison.errorRateDeltaPercent() >= 3 ? -1 : 0);
        }

        if (score >= 2) {
            return "IMPROVING";
        }
        if (score <= -2) {
            return "DECLINING";
        }
        return "STABLE";
    }

    private String anonymizeParticipantId(Long participantId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(("mindmetrics-anon-" + participantId).getBytes(StandardCharsets.UTF_8));
            return "P-" + HexFormat.of().formatHex(hash).substring(0, 12);
        } catch (Exception ex) {
            return "P-" + participantId;
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String resolveTaskTitle(String taskType) {
        try {
            return taskCatalogService.getTaskById(taskType).getTitle();
        } catch (BadRequestException ex) {
            return taskType;
        }
    }
}
