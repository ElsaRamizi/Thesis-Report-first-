package com.example.demo.service;

import com.example.demo.analytics.CognitiveMetricsCalculator;
import com.example.demo.analytics.CognitiveMetricsCalculator.CognitiveMetricSnapshot;
import com.example.demo.analytics.CognitiveMetricsCalculator.CognitiveProfile;
import com.example.demo.analytics.CognitiveMetricsCalculator.CognitiveTimelinePoint;
import com.example.demo.analytics.CognitiveMetricsCalculator.CohortStatistics;
import com.example.demo.analytics.CognitiveMetricsCalculator.MetricComparison;
import com.example.demo.analytics.CognitiveMetricsCalculator.SessionAnalysis;
import com.example.demo.dto.CohortAnalyticsRequest;
import com.example.demo.dto.CohortAnalyticsResponse;
import com.example.demo.dto.CohortComparisonGroupDto;
import com.example.demo.dto.CohortStatisticsDto;
import com.example.demo.dto.CognitiveMetricsDto;
import com.example.demo.dto.CognitiveProfileDto;
import com.example.demo.dto.CognitiveTimelinePointDto;
import com.example.demo.dto.DistributionBucketDto;
import com.example.demo.dto.HistogramBucketDto;
import com.example.demo.dto.MetricComparisonDto;
import com.example.demo.dto.ParticipantAnalyticsResponse;
import com.example.demo.dto.SessionAnalysisDto;
import com.example.demo.dto.SessionAnalyticsDto;
import com.example.demo.dto.SharedPatientSummary;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.DoctorConnection;
import com.example.demo.model.ResearchCohort;
import com.example.demo.model.ResearchParticipation;
import com.example.demo.model.ResearchStudy;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import com.example.demo.model.User;
import com.example.demo.repository.AggregatedMetricsRepository;
import com.example.demo.repository.DoctorConnectionRepository;
import com.example.demo.repository.ParticipantProfileRepository;
import com.example.demo.repository.ResearchCohortRepository;
import com.example.demo.repository.ResearchParticipationRepository;
import com.example.demo.repository.ResearchStudyRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.TrialDataRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CognitiveAnalyticsService {

    private final UserRepository userRepository;
    private final TestSessionRepository testSessionRepository;
    private final AggregatedMetricsRepository aggregatedMetricsRepository;
    private final TrialDataRepository trialDataRepository;
    private final DoctorConnectionRepository doctorConnectionRepository;
    private final ResearchParticipationRepository participationRepository;
    private final ResearchStudyRepository studyRepository;
    private final ResearchCohortRepository cohortRepository;
    private final ResearchService researchService;
    private final TaskCatalogService taskCatalogService;
    private final ObjectMapper objectMapper;
    private final ClinicianAccessService clinicianAccessService;
    private final ParticipantProfileRepository participantProfileRepository;

    public CognitiveAnalyticsService(
        UserRepository userRepository,
        TestSessionRepository testSessionRepository,
        AggregatedMetricsRepository aggregatedMetricsRepository,
        TrialDataRepository trialDataRepository,
        DoctorConnectionRepository doctorConnectionRepository,
        ResearchParticipationRepository participationRepository,
        ResearchStudyRepository studyRepository,
        ResearchCohortRepository cohortRepository,
        ResearchService researchService,
        TaskCatalogService taskCatalogService,
        ObjectMapper objectMapper,
        ClinicianAccessService clinicianAccessService,
        ParticipantProfileRepository participantProfileRepository
    ) {
        this.userRepository = userRepository;
        this.testSessionRepository = testSessionRepository;
        this.aggregatedMetricsRepository = aggregatedMetricsRepository;
        this.trialDataRepository = trialDataRepository;
        this.doctorConnectionRepository = doctorConnectionRepository;
        this.participationRepository = participationRepository;
        this.studyRepository = studyRepository;
        this.cohortRepository = cohortRepository;
        this.researchService = researchService;
        this.taskCatalogService = taskCatalogService;
        this.objectMapper = objectMapper;
        this.clinicianAccessService = clinicianAccessService;
        this.participantProfileRepository = participantProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<SharedPatientSummary> getSharedPatients(String clinicianEmail) {
        clinicianAccessService.findClinician(clinicianEmail);
        return clinicianAccessService.getAccessibleParticipants(clinicianEmail).stream()
            .map(participant -> new SharedPatientSummary(
                participant.getId(),
                participant.getEmail(),
                false,
                "CLINICIAN_ACCESS",
                participant.getCreatedAt(),
                null,
                null
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public ParticipantAnalyticsResponse getParticipantAnalytics(String clinicianEmail, Long participantId) {
        User clinician = findClinician(clinicianEmail);
        User participant = clinicianAccessService.requireAccessibleParticipant(clinicianEmail, participantId);
        SharingContext context = resolveSharingContext(clinician, participant);

        List<SessionBundle> bundles = loadSessionBundles(participant, context.allowedTaskTypes());
        if (bundles.isEmpty()) {
            throw new BadRequestException("No gameplay data is available for this participant under current sharing permissions.");
        }

        List<CognitiveMetricSnapshot> chronological = bundles.stream()
            .map(bundle -> CognitiveMetricsCalculator.snapshotFromMetrics(bundle.metrics()))
            .toList();

        List<CognitiveMetricSnapshot> mutableChronological = new ArrayList<>(chronological);
        Double improvementRate = CognitiveMetricsCalculator.computeImprovementRate(mutableChronological);
        CognitiveMetricSnapshot latest = withImprovement(mutableChronological.get(mutableChronological.size() - 1), improvementRate);
        CognitiveMetricSnapshot overallAverage = CognitiveMetricsCalculator.averageSnapshots(mutableChronological);

        List<CognitiveTimelinePoint> timeline = buildTimeline(bundles);
        List<CognitiveTimelinePoint> rollingTimeline = CognitiveMetricsCalculator.buildRollingAverage(timeline, 3);
        CognitiveMetricSnapshot rollingAverage = CognitiveMetricsCalculator.averageSnapshots(
            rollingTimeline.stream()
                .map(point -> new CognitiveMetricSnapshot(
                    point.avgReactionTime(),
                    null,
                    point.accuracy(),
                    point.errorRate(),
                    null,
                    point.missRate(),
                    null,
                    null,
                    null
                ))
                .toList()
        );

        List<User> cohortParticipants = clinicianAccessService.getAccessibleParticipants(clinicianEmail);
        List<CognitiveMetricSnapshot> cohortSnapshots = cohortParticipants.stream()
            .flatMap(user -> loadSessionBundles(user, null).stream())
            .map(bundle -> CognitiveMetricsCalculator.snapshotFromMetrics(bundle.metrics()))
            .toList();
        CognitiveMetricSnapshot cohortAverage = CognitiveMetricsCalculator.averageSnapshots(cohortSnapshots);
        MetricComparison comparison = CognitiveMetricsCalculator.compareToBaseline(latest, cohortAverage);
        CognitiveProfile profile = CognitiveMetricsCalculator.buildProfile(latest);

        return new ParticipantAnalyticsResponse(
            participant.getId(),
            context.displayName(),
            context.anonymous(),
            context.source(),
            toMetricsDto(latest),
            toMetricsDto(overallAverage),
            toMetricsDto(rollingAverage),
            toMetricsDto(cohortAverage),
            toComparisonDto(comparison),
            toProfileDto(profile),
            timeline.stream().map(this::toTimelineDto).toList(),
            rollingTimeline.stream().map(this::toTimelineDto).toList(),
            bundles.stream().map(this::toSessionAnalytics).toList()
        );
    }

    @Transactional(readOnly = true)
    public CohortAnalyticsResponse getCohortAnalytics(String clinicianEmail, CohortAnalyticsRequest request) {
        findClinician(clinicianEmail);
        List<User> participants = resolveCohortParticipants(clinicianEmail, request);
        List<CognitiveMetricSnapshot> snapshots = participants.stream()
            .flatMap(participant -> loadSessionBundles(participant, null).stream())
            .map(bundle -> CognitiveMetricsCalculator.snapshotFromMetrics(bundle.metrics()))
            .toList();

        CohortStatistics stats = CognitiveMetricsCalculator.cohortStatistics(snapshots);
        List<CognitiveTimelinePointDto> cohortTimeline = buildCohortTimeline(participants);
        List<HistogramBucketDto> histogram = buildHistogram(participants);

        List<CohortComparisonGroupDto> comparisonGroups = new ArrayList<>();
        if (request.filterGroups() != null) {
            int index = 1;
            for (var filterGroup : request.filterGroups()) {
                if (request.studyId() == null) {
                    continue;
                }
                List<User> filtered = researchService.filterParticipantsByCriteria(request.studyId(), clinicianEmail, filterGroup);
                comparisonGroups.add(buildComparisonGroup("Filter Group " + index++, filtered));
            }
        }

        return new CohortAnalyticsResponse(
            participants.size(),
            toMetricsDto(CognitiveMetricsCalculator.averageSnapshots(snapshots)),
            toStatisticsDto(stats),
            cohortTimeline,
            histogram,
            comparisonGroups
        );
    }

    private CohortComparisonGroupDto buildComparisonGroup(String label, List<User> participants) {
        List<CognitiveMetricSnapshot> snapshots = participants.stream()
            .flatMap(participant -> loadSessionBundles(participant, null).stream())
            .map(bundle -> CognitiveMetricsCalculator.snapshotFromMetrics(bundle.metrics()))
            .toList();
        return new CohortComparisonGroupDto(label, participants.size(), toMetricsDto(CognitiveMetricsCalculator.averageSnapshots(snapshots)));
    }

    private List<User> resolveCohortParticipants(String clinicianEmail, CohortAnalyticsRequest request) {
        if (request.participantIds() != null && !request.participantIds().isEmpty()) {
            return request.participantIds().stream()
                .map(this::findParticipant)
                .toList();
        }
        if (request.studyId() != null && request.cohortIds() != null && !request.cohortIds().isEmpty()) {
            ResearchStudy study = studyRepository.findById(request.studyId())
                .orElseThrow(() -> new BadRequestException("Study was not found."));
            if (!study.getCreator().getEmail().equals(clinicianEmail)) {
                throw new UnauthorizedException("You do not have access to this study.");
            }
            List<User> participants = new ArrayList<>();
            for (Long cohortId : request.cohortIds()) {
                ResearchCohort cohort = cohortRepository.findById(cohortId)
                    .orElseThrow(() -> new BadRequestException("Cohort was not found."));
                researchService.filterParticipationsForStudy(study, parseFilters(cohort.getFilterCriteriaJson())).stream()
                    .map(ResearchParticipation::getParticipant)
                    .forEach(participants::add);
            }
            return participants.stream().distinct().toList();
        }
        if (request.studyId() != null) {
            ResearchStudy study = studyRepository.findById(request.studyId())
                .orElseThrow(() -> new BadRequestException("Study was not found."));
            return participationRepository.findByStudyOrderByJoinedAtDesc(study).stream()
                .filter(participation -> participation.isDataSharingAccepted())
                .filter(participation -> !ResearchService.PARTICIPATION_WITHDRAWN.equals(participation.getStatus()))
                .map(ResearchParticipation::getParticipant)
                .distinct()
                .toList();
        }

        return doctorConnectionRepository.findByDoctorEmailIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(clinicianEmail).stream()
            .map(DoctorConnection::getPatient)
            .distinct()
            .toList();
    }

    private List<SessionBundle> loadSessionBundles(User participant, List<String> allowedTaskTypes) {
        List<TestSession> sessions = testSessionRepository.findByUserIdOrderByStartTimeDesc(participant);
        List<SessionBundle> bundles = new ArrayList<>();
        for (TestSession session : sessions) {
            if (allowedTaskTypes != null && !allowedTaskTypes.isEmpty() && !allowedTaskTypes.contains(session.getTaskType())) {
                continue;
            }
            Optional<AggregatedMetrics> metricsOpt = aggregatedMetricsRepository.findBySessionId(session);
            if (metricsOpt.isEmpty()) {
                continue;
            }
            List<TrialData> trials = trialDataRepository.findBySessionIdOrderByTimestampAsc(session);
            bundles.add(new SessionBundle(session, metricsOpt.get(), trials));
        }
        bundles.sort(Comparator.comparing(bundle -> bundle.session().getStartTime()));
        return bundles;
    }

    private SharingContext resolveSharingContext(User clinician, User participant) {
        if (participantProfileRepository.findByUser(participant)
            .map(profile -> profile.getAssignedClinician())
            .filter(assigned -> assigned.getId().equals(clinician.getId()))
            .isPresent()) {
            return new SharingContext(participant.getEmail(), false, "ASSIGNED_CLINICIAN", null);
        }

        Optional<DoctorConnection> connection = doctorConnectionRepository
            .findByDoctorEmailIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(clinician.getEmail()).stream()
            .filter(item -> item.getPatient().getId().equals(participant.getId()))
            .findFirst();

        if (connection.isPresent()) {
            DoctorConnection active = connection.get();
            if (!active.isShareAnalyticsOnly() && !active.isShareFullIdentifiable() && !active.isShareAnonymizedOnly()) {
                throw new UnauthorizedException("This doctor does not have analytics sharing permission.");
            }
            List<String> allowedGames = active.isShareSelectedGamesOnly()
                ? parseGames(active.getSelectedGamesJson())
                : null;
            String displayName = active.isUseAnonymousSharing() || active.isShareAnonymizedOnly()
                ? active.getAnonymousIdentifier()
                : participant.getEmail();
            return new SharingContext(
                displayName,
                active.isUseAnonymousSharing() || active.isShareAnonymizedOnly(),
                "DOCTOR_CONNECTION",
                allowedGames
            );
        }

        Optional<ResearchParticipation> researchShare = participationRepository.findByParticipantOrderByJoinedAtDesc(participant).stream()
            .filter(participation -> participation.isDataSharingAccepted())
            .filter(participation -> !ResearchService.PARTICIPATION_WITHDRAWN.equals(participation.getStatus()))
            .filter(participation -> participation.getStudy().getCreator().getId().equals(clinician.getId()))
            .findFirst();

        if (researchShare.isPresent()) {
            ResearchParticipation participation = researchShare.get();
            String displayName = participation.isAnonymous()
                ? participation.getAnonymousIdentifier()
                : participant.getEmail();
            return new SharingContext(displayName, participation.isAnonymous(), "RESEARCH_STUDY", null);
        }

        throw new UnauthorizedException("You do not have access to this participant's analytics.");
    }

    private List<CognitiveTimelinePoint> buildTimeline(List<SessionBundle> bundles) {
        List<CognitiveTimelinePoint> timeline = new ArrayList<>();
        for (int index = 0; index < bundles.size(); index++) {
            SessionBundle bundle = bundles.get(index);
            AggregatedMetrics metrics = bundle.metrics();
            timeline.add(new CognitiveTimelinePoint(
                bundle.session().getId(),
                "Session " + (index + 1),
                bundle.session().getTaskType(),
                bundle.session().getStartTime(),
                metrics.getAvgReactionTime(),
                metrics.getAccuracy(),
                metrics.getErrorRate(),
                metrics.getMissRate(),
                false
            ));
        }
        return timeline;
    }

    private List<CognitiveTimelinePointDto> buildCohortTimeline(List<User> participants) {
        Map<String, List<Double>> grouped = new LinkedHashMap<>();
        for (User participant : participants) {
            List<SessionBundle> bundles = loadSessionBundles(participant, null);
            for (int index = 0; index < bundles.size(); index++) {
                String key = "Session " + (index + 1);
                grouped.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(bundles.get(index).metrics().getAvgReactionTime());
            }
        }

        List<CognitiveTimelinePointDto> timeline = new ArrayList<>();
        grouped.forEach((label, values) -> timeline.add(new CognitiveTimelinePointDto(
            null,
            label,
            "cohort-average",
            null,
            CognitiveMetricsCalculator.averageSnapshots(values.stream().map(value -> new CognitiveMetricSnapshot(value, null, null, null, null, null, null, null, null)).toList()).avgReactionTime(),
            null,
            null,
            null,
            true
        )));
        return timeline;
    }

    private List<HistogramBucketDto> buildHistogram(List<User> participants) {
        List<Long> reactionTimes = participants.stream()
            .flatMap(participant -> loadSessionBundles(participant, null).stream())
            .map(bundle -> bundle.metrics().getAvgReactionTime())
            .filter(Objects::nonNull)
            .map(Math::round)
            .toList();

        long[] buckets = new long[5];
        for (Long value : reactionTimes) {
            int bucket = (int) Math.min(4, value / 250);
            buckets[bucket]++;
        }
        List<HistogramBucketDto> histogram = new ArrayList<>();
        for (int index = 0; index < buckets.length; index++) {
            histogram.add(new HistogramBucketDto((index * 250) + "-" + ((index + 1) * 250) + " ms", buckets[index]));
        }
        return histogram;
    }

    private SessionAnalyticsDto toSessionAnalytics(SessionBundle bundle) {
        SessionAnalysis analysis = CognitiveMetricsCalculator.analyzeSession(bundle.trials());
        return new SessionAnalyticsDto(
            bundle.session().getId(),
            bundle.session().getTaskType(),
            resolveTaskTitle(bundle.session().getTaskType()),
            bundle.session().getStartTime(),
            toMetricsDto(CognitiveMetricsCalculator.snapshotFromMetrics(bundle.metrics())),
            new SessionAnalysisDto(
                analysis.totalTrials(),
                analysis.incorrectResponses(),
                analysis.reactionTimeDistribution().stream().map(bucket -> new DistributionBucketDto(bucket.label(), bucket.count())).toList(),
                analysis.fatigueIndicatorPercent(),
                analysis.anomalies()
            )
        );
    }

    private CognitiveMetricSnapshot withImprovement(CognitiveMetricSnapshot snapshot, Double improvementRate) {
        return new CognitiveMetricSnapshot(
            snapshot.avgReactionTime(),
            snapshot.medianReactionTime(),
            snapshot.accuracy(),
            snapshot.errorRate(),
            snapshot.falseAlarmRate(),
            snapshot.missRate(),
            snapshot.maxNReached(),
            improvementRate,
            snapshot.responseVariability()
        );
    }

    private SharedPatientSummary toSharedSummaryFromConnection(DoctorConnection connection) {
        String displayName = connection.isUseAnonymousSharing() || connection.isShareAnonymizedOnly()
            ? connection.getAnonymousIdentifier()
            : connection.getPatient().getEmail();
        return new SharedPatientSummary(
            connection.getPatient().getId(),
            displayName,
            connection.isUseAnonymousSharing() || connection.isShareAnonymizedOnly(),
            "DOCTOR_CONNECTION",
            connection.getCreatedAt(),
            null,
            null
        );
    }

    private SharedPatientSummary toSharedSummaryFromResearch(ResearchParticipation participation, ResearchStudy study) {
        String displayName = participation.isAnonymous()
            ? participation.getAnonymousIdentifier()
            : participation.getParticipant().getEmail();
        return new SharedPatientSummary(
            participation.getParticipant().getId(),
            displayName,
            participation.isAnonymous(),
            "RESEARCH_STUDY",
            participation.getJoinedAt(),
            study.getId(),
            study.getTitle()
        );
    }

    private CognitiveMetricsDto toMetricsDto(CognitiveMetricSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new CognitiveMetricsDto(
            snapshot.avgReactionTime(),
            snapshot.medianReactionTime(),
            snapshot.accuracy(),
            snapshot.errorRate(),
            snapshot.falseAlarmRate(),
            snapshot.missRate(),
            snapshot.maxNReached(),
            snapshot.improvementRate(),
            snapshot.responseVariability()
        );
    }

    private MetricComparisonDto toComparisonDto(MetricComparison comparison) {
        String summary = buildComparisonSummary(comparison);
        return new MetricComparisonDto(
            comparison.reactionTimeDeltaPercent(),
            comparison.accuracyDeltaPercent(),
            comparison.errorRateDeltaPercent(),
            comparison.missRateDeltaPercent(),
            summary
        );
    }

    private String buildComparisonSummary(MetricComparison comparison) {
        if (comparison.reactionTimeDeltaPercent() == null && comparison.accuracyDeltaPercent() == null) {
            return "Not enough data to compare with the group.";
        }
        List<String> parts = new ArrayList<>();
        if (comparison.reactionTimeDeltaPercent() != null) {
            parts.add("reaction time is " + Math.abs(comparison.reactionTimeDeltaPercent()) + "% "
                + (comparison.reactionTimeDeltaPercent() >= 0 ? "faster" : "slower") + " than cohort average");
        }
        if (comparison.accuracyDeltaPercent() != null) {
            parts.add("accuracy is " + Math.abs(comparison.accuracyDeltaPercent()) + "% "
                + (comparison.accuracyDeltaPercent() >= 0 ? "higher" : "lower") + " than cohort average");
        }
        return "Participant " + String.join(" and ", parts) + ".";
    }

    private CognitiveProfileDto toProfileDto(CognitiveProfile profile) {
        return new CognitiveProfileDto(
            profile.memory(),
            profile.reactionSpeed(),
            profile.attention(),
            profile.consistency(),
            profile.inhibitionControl(),
            profile.adaptability()
        );
    }

    private CognitiveTimelinePointDto toTimelineDto(CognitiveTimelinePoint point) {
        return new CognitiveTimelinePointDto(
            point.sessionId(),
            point.label(),
            point.taskType(),
            point.startTime(),
            point.avgReactionTime(),
            point.accuracy(),
            point.errorRate(),
            point.missRate(),
            point.rolling()
        );
    }

    private CohortStatisticsDto toStatisticsDto(CohortStatistics stats) {
        return new CohortStatisticsDto(
            stats.participantCount(),
            stats.avgReactionTime(),
            stats.medianReactionTime(),
            stats.avgAccuracy(),
            stats.avgErrorRate(),
            stats.avgFalseAlarmRate(),
            stats.avgMaxNReached(),
            stats.reactionTimeStdDev(),
            stats.accuracyVariance()
        );
    }

    private User findClinician(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User was not found."));
        if (!"CLINICIAN".equals(user.getRole())) {
            throw new UnauthorizedException("Clinician access is required.");
        }
        return user;
    }

    private User findParticipant(Long participantId) {
        User participant = userRepository.findById(participantId)
            .orElseThrow(() -> new BadRequestException("Participant was not found."));
        if (!"USER".equals(participant.getRole())) {
            throw new BadRequestException("Only participant accounts can be analyzed here.");
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

    private List<String> parseGames(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<com.example.demo.dto.ResearchFilterCriterion> parseFilters(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<com.example.demo.dto.ResearchFilterCriterion>>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }

    private record SessionBundle(TestSession session, AggregatedMetrics metrics, List<TrialData> trials) {}

    private record SharingContext(String displayName, boolean anonymous, String source, List<String> allowedTaskTypes) {}
}
