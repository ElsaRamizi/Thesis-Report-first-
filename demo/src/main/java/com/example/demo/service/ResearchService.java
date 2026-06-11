package com.example.demo.service;

import com.example.demo.dto.ResearchAnswerRequest;
import com.example.demo.dto.ResearchCohortComparison;
import com.example.demo.dto.ResearchCohortRequest;
import com.example.demo.dto.ResearchCohortResponse;
import com.example.demo.dto.ResearchCompareRequest;
import com.example.demo.dto.ResearchCompareResponse;
import com.example.demo.dto.ResearchFilterCriterion;
import com.example.demo.dto.ResearchFilterPreviewRequest;
import com.example.demo.dto.ResearchGameCorrelation;
import com.example.demo.dto.ResearchJoinRequest;
import com.example.demo.dto.ResearchParticipantSummary;
import com.example.demo.dto.ResearchParticipationResponse;
import com.example.demo.dto.ResearchQuestionAnalytics;
import com.example.demo.dto.ResearchQuestionRequest;
import com.example.demo.dto.ResearchQuestionResponse;
import com.example.demo.dto.ResearchStudyDetailResponse;
import com.example.demo.dto.ResearchStudyRequest;
import com.example.demo.dto.ResearchStudyResponse;
import com.example.demo.dto.ResearchTrendPoint;
import com.example.demo.dto.ResearchAnalyticsResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.ResearchAnswer;
import com.example.demo.model.ResearchCohort;
import com.example.demo.model.ResearchParticipation;
import com.example.demo.model.ResearchQuestion;
import com.example.demo.model.ResearchStudy;
import com.example.demo.model.TestSession;
import com.example.demo.model.User;
import com.example.demo.repository.AggregatedMetricsRepository;
import com.example.demo.repository.ResearchAnswerRepository;
import com.example.demo.repository.ResearchCohortRepository;
import com.example.demo.repository.ResearchParticipationRepository;
import com.example.demo.repository.ResearchQuestionRepository;
import com.example.demo.repository.ResearchStudyRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResearchService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_CLOSED = "CLOSED";

    public static final String PARTICIPATION_ENROLLED = "ENROLLED";
    public static final String PARTICIPATION_IN_PROGRESS = "IN_PROGRESS";
    public static final String PARTICIPATION_COMPLETED = "COMPLETED";
    public static final String PARTICIPATION_WITHDRAWN = "WITHDRAWN";

    public static final String KEY_DOB = "DEMOGRAPHIC_DOB";
    public static final String KEY_GENDER = "DEMOGRAPHIC_GENDER";

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "ONLINE_TESTING", "IN_PERSON_TESTING"
    );

    private static final Set<String> ALLOWED_QUESTION_TYPES = Set.of(
        "SHORT_TEXT", "LONG_TEXT", "MULTIPLE_CHOICE", "SINGLE_CHOICE",
        "YES_NO", "DROPDOWN", "NUMERIC"
    );

    private final ResearchStudyRepository studyRepository;
    private final ResearchQuestionRepository questionRepository;
    private final ResearchParticipationRepository participationRepository;
    private final ResearchAnswerRepository answerRepository;
    private final ResearchCohortRepository cohortRepository;
    private final UserRepository userRepository;
    private final TestSessionRepository testSessionRepository;
    private final AggregatedMetricsRepository aggregatedMetricsRepository;
    private final ObjectMapper objectMapper;

    public ResearchService(
        ResearchStudyRepository studyRepository,
        ResearchQuestionRepository questionRepository,
        ResearchParticipationRepository participationRepository,
        ResearchAnswerRepository answerRepository,
        ResearchCohortRepository cohortRepository,
        UserRepository userRepository,
        TestSessionRepository testSessionRepository,
        AggregatedMetricsRepository aggregatedMetricsRepository,
        ObjectMapper objectMapper
    ) {
        this.studyRepository = studyRepository;
        this.questionRepository = questionRepository;
        this.participationRepository = participationRepository;
        this.answerRepository = answerRepository;
        this.cohortRepository = cohortRepository;
        this.userRepository = userRepository;
        this.testSessionRepository = testSessionRepository;
        this.aggregatedMetricsRepository = aggregatedMetricsRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ResearchStudyResponse> browsePublishedStudies() {
        return studyRepository.findByStatusOrderByCreatedAtDesc(STATUS_PUBLISHED).stream()
            .map(this::toStudyResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ResearchStudyDetailResponse getPublishedStudyDetail(Long studyId) {
        ResearchStudy study = findPublishedStudy(studyId);
        return toStudyDetailResponse(study);
    }

    @Transactional(readOnly = true)
    public List<ResearchParticipationResponse> getMyParticipations(String email) {
        User participant = findUser(email);
        return participationRepository.findByParticipantOrderByJoinedAtDesc(participant).stream()
            .map(this::toParticipationResponse)
            .toList();
    }

    @Transactional
    public ResearchParticipationResponse joinStudy(Long studyId, String email, ResearchJoinRequest request) {
        User participant = findUser(email);
        ResearchStudy study = findPublishedStudy(studyId);

        if (!study.isAnonymousFriendly() && request.anonymous()) {
            throw new BadRequestException("This study does not support anonymous participation.");
        }

        if (!request.consentAccepted()) {
            throw new BadRequestException("Consent must be accepted to join a study.");
        }

        if (!request.dataSharingAccepted()) {
            throw new BadRequestException(
                "You must consent to your anonymized or identified responses being used for research analytics."
            );
        }

        Optional<ResearchParticipation> existingParticipation =
            participationRepository.findByStudyAndParticipant(study, participant);

        if (existingParticipation.isPresent()) {
            ResearchParticipation existing = existingParticipation.get();
            if (!PARTICIPATION_WITHDRAWN.equals(existing.getStatus())) {
                throw new ConflictException("You are already enrolled in this study.");
            }

            existing.setAnonymous(request.anonymous());
            existing.setAnonymousIdentifier(request.anonymous()
                ? "ANON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : null);
            existing.setConsentAccepted(true);
            existing.setDataSharingAccepted(true);
            existing.setStatus(PARTICIPATION_ENROLLED);
            existing.setProgressPercent(0);
            existing.setJoinedAt(LocalDateTime.now());
            existing.setWithdrawnAt(null);
            existing.setCompletedAt(null);

            ResearchParticipation rejoined = participationRepository.save(existing);
            saveAnswers(rejoined, request.answers());
            updateProgress(rejoined);
            return toParticipationResponse(rejoined);
        }

        ResearchParticipation participation = new ResearchParticipation();
        participation.setStudy(study);
        participation.setParticipant(participant);
        participation.setAnonymous(request.anonymous());
        participation.setAnonymousIdentifier(request.anonymous() ? "ANON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : null);
        participation.setConsentAccepted(true);
        participation.setDataSharingAccepted(true);
        participation.setStatus(PARTICIPATION_ENROLLED);
        participation.setProgressPercent(0);
        participation.setJoinedAt(LocalDateTime.now());

        ResearchParticipation saved = participationRepository.save(participation);
        saveAnswers(saved, request.answers());
        updateProgress(saved);
        return toParticipationResponse(saved);
    }

    @Transactional
    public ResearchParticipationResponse withdrawParticipation(Long participationId, String email) {
        ResearchParticipation participation = findOwnedParticipation(participationId, email);
        if (PARTICIPATION_WITHDRAWN.equals(participation.getStatus())) {
            throw new BadRequestException("Participation is already withdrawn.");
        }
        participation.setStatus(PARTICIPATION_WITHDRAWN);
        participation.setWithdrawnAt(LocalDateTime.now());
        return toParticipationResponse(participationRepository.save(participation));
    }

    @Transactional
    public ResearchParticipationResponse updateParticipationAnswers(
        Long participationId,
        String email,
        List<ResearchAnswerRequest> answers
    ) {
        ResearchParticipation participation = findOwnedParticipation(participationId, email);
        if (PARTICIPATION_WITHDRAWN.equals(participation.getStatus())) {
            throw new BadRequestException("Withdrawn participations cannot be updated.");
        }
        saveAnswers(participation, answers);
        updateProgress(participation);
        return toParticipationResponse(participation);
    }

    @Transactional(readOnly = true)
    public List<ResearchStudyResponse> getClinicianStudies(String email) {
        User clinician = findClinician(email);
        return studyRepository.findByCreatorOrderByCreatedAtDesc(clinician).stream()
            .map(this::toStudyResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ResearchStudyDetailResponse getClinicianStudyDetail(Long studyId, String email) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        return toStudyDetailResponse(study);
    }

    @Transactional
    public ResearchStudyDetailResponse createStudy(String email, ResearchStudyRequest request) {
        User clinician = findClinician(email);
        validateStudyRequest(request);

        ResearchStudy study = new ResearchStudy();
        study.setCreator(clinician);
        applyStudyFields(study, request);
        study.setStatus(STATUS_DRAFT);
        study.setCreatedAt(LocalDateTime.now());
        study.setUpdatedAt(LocalDateTime.now());

        ResearchStudy saved = studyRepository.save(study);
        createDefaultDemographicQuestions(saved);
        addCustomQuestions(saved, request.customQuestions(), 2);
        return toStudyDetailResponse(saved);
    }

    @Transactional
    public ResearchStudyDetailResponse updateStudy(Long studyId, String email, ResearchStudyRequest request) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        validateStudyRequest(request);
        applyStudyFields(study, request);
        study.setUpdatedAt(LocalDateTime.now());
        return toStudyDetailResponse(studyRepository.save(study));
    }

    @Transactional
    public ResearchStudyDetailResponse publishStudy(Long studyId, String email) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        study.setStatus(STATUS_PUBLISHED);
        study.setUpdatedAt(LocalDateTime.now());
        return toStudyDetailResponse(studyRepository.save(study));
    }

    @Transactional
    public void deleteStudy(Long studyId, String email) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        studyRepository.delete(study);
    }

    @Transactional
    public ResearchStudyDetailResponse addQuestion(Long studyId, String email, ResearchQuestionRequest request) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        validateQuestionRequest(request);

        List<ResearchQuestion> existing = questionRepository.findByStudyOrderBySortOrderAsc(study);
        int nextOrder = existing.stream().mapToInt(ResearchQuestion::getSortOrder).max().orElse(0) + 1;

        ResearchQuestion question = buildQuestion(study, request, nextOrder, null, false);
        questionRepository.save(question);
        return toStudyDetailResponse(study);
    }

    @Transactional
    public ResearchStudyDetailResponse updateQuestion(Long questionId, String email, ResearchQuestionRequest request) {
        ResearchQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new BadRequestException("Question was not found."));
        findOwnedStudy(question.getStudy().getId(), email);

        if (question.isDemographicDefault()) {
            throw new BadRequestException("Default demographic questions cannot be edited.");
        }

        validateQuestionRequest(request);
        question.setQuestionText(request.questionText());
        question.setQuestionType(normalizeQuestionType(request.questionType()));
        question.setOptionsJson(toOptionsJson(request.options()));
        question.setRequired(request.required());
        question.setSortOrder(request.sortOrder());
        questionRepository.save(question);
        return toStudyDetailResponse(question.getStudy());
    }

    @Transactional
    public ResearchStudyDetailResponse deleteQuestion(Long questionId, String email) {
        ResearchQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new BadRequestException("Question was not found."));
        findOwnedStudy(question.getStudy().getId(), email);

        if (question.isDemographicDefault()) {
            throw new BadRequestException("Default demographic questions cannot be deleted.");
        }

        questionRepository.delete(question);
        return toStudyDetailResponse(question.getStudy());
    }

    @Transactional
    public ResearchCohortResponse createCohort(Long studyId, String email, ResearchCohortRequest request) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        validateCohortRequest(request);

        ResearchCohort cohort = new ResearchCohort();
        cohort.setStudy(study);
        cohort.setName(request.name().trim());
        cohort.setDescription(request.description());
        cohort.setFilterCriteriaJson(toFilterJson(request.filters()));
        cohort.setCreatedAt(LocalDateTime.now());

        ResearchCohort saved = cohortRepository.save(cohort);
        return toCohortResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ResearchCohortResponse> getCohorts(Long studyId, String email) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        return cohortRepository.findByStudyOrderByCreatedAtDesc(study).stream()
            .map(this::toCohortResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ResearchParticipantSummary> previewFilter(Long studyId, String email, ResearchFilterPreviewRequest request) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        List<ResearchParticipation> matched = filterParticipations(study, request.filters());
        return matched.stream().map(this::toParticipantSummary).toList();
    }

    @Transactional(readOnly = true)
    public ResearchAnalyticsResponse getAnalytics(Long studyId, String email, List<ResearchFilterCriterion> filters) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        List<ResearchParticipation> participations = filterParticipations(study, filters);
        return buildAnalytics(study, participations);
    }

    @Transactional(readOnly = true)
    public ResearchCompareResponse compareCohorts(Long studyId, String email, ResearchCompareRequest request) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        List<ResearchCohortComparison> groups = new ArrayList<>();

        if (request.cohortIds() != null) {
            for (Long cohortId : request.cohortIds()) {
                ResearchCohort cohort = cohortRepository.findById(cohortId)
                    .orElseThrow(() -> new BadRequestException("Cohort was not found."));
                if (!cohort.getStudy().getId().equals(study.getId())) {
                    throw new BadRequestException("Cohort does not belong to this study.");
                }
                List<ResearchParticipation> matched = filterParticipations(study, parseFilters(cohort.getFilterCriteriaJson()));
                ResearchAnalyticsResponse analytics = buildAnalytics(study, matched);
                groups.add(new ResearchCohortComparison(
                    cohort.getName(),
                    matched.size(),
                    analytics.questionAnalytics(),
                    analytics.gameCorrelation()
                ));
            }
        }

        if (request.filterGroups() != null) {
            int index = 1;
            for (List<ResearchFilterCriterion> filterGroup : request.filterGroups()) {
                List<ResearchParticipation> matched = filterParticipations(study, filterGroup);
                ResearchAnalyticsResponse analytics = buildAnalytics(study, matched);
                groups.add(new ResearchCohortComparison(
                    "Filter Group " + index++,
                    matched.size(),
                    analytics.questionAnalytics(),
                    analytics.gameCorrelation()
                ));
            }
        }

        return new ResearchCompareResponse(groups);
    }

    @Transactional(readOnly = true)
    public String exportAnonymizedData(Long studyId, String email, List<ResearchFilterCriterion> filters) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        List<ResearchParticipation> participations = filterParticipations(study, filters);
        List<ResearchQuestion> questions = questionRepository.findByStudyOrderBySortOrderAsc(study);

        StringBuilder csv = new StringBuilder();
        csv.append("participant_id,anonymous,status,joined_at");
        for (ResearchQuestion question : questions) {
            csv.append(',').append(escapeCsv(question.getQuestionText()));
        }
        csv.append('\n');

        for (ResearchParticipation participation : participations) {
            Map<Long, String> answers = loadAnswerMap(participation);
            csv.append(escapeCsv(participation.isAnonymous()
                ? participation.getAnonymousIdentifier()
                : "P-" + participation.getId()));
            csv.append(',').append(participation.isAnonymous());
            csv.append(',').append(participation.getStatus());
            csv.append(',').append(participation.getJoinedAt());
            for (ResearchQuestion question : questions) {
                csv.append(',').append(escapeCsv(answers.getOrDefault(question.getId(), "")));
            }
            csv.append('\n');
        }

        return csv.toString();
    }

    private ResearchAnalyticsResponse buildAnalytics(ResearchStudy study, List<ResearchParticipation> participations) {
        int anonymousCount = (int) participations.stream().filter(ResearchParticipation::isAnonymous).count();
        Map<String, Integer> statusBreakdown = participations.stream()
            .collect(Collectors.groupingBy(ResearchParticipation::getStatus, Collectors.summingInt(p -> 1)));

        List<ResearchQuestion> questions = questionRepository.findByStudyOrderBySortOrderAsc(study);
        List<ResearchQuestionAnalytics> questionAnalytics = questions.stream()
            .map(question -> buildQuestionAnalytics(question, participations))
            .toList();

        ResearchGameCorrelation gameCorrelation = buildGameCorrelation(participations);
        List<ResearchTrendPoint> trend = buildEnrollmentTrend(participations);
        List<ResearchParticipantSummary> participantSummaries = participations.stream()
            .map(this::toParticipantSummary)
            .toList();

        return new ResearchAnalyticsResponse(
            participations.size(),
            anonymousCount,
            participations.size() - anonymousCount,
            statusBreakdown,
            questionAnalytics,
            gameCorrelation,
            trend,
            participantSummaries
        );
    }

    private ResearchQuestionAnalytics buildQuestionAnalytics(ResearchQuestion question, List<ResearchParticipation> participations) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (ResearchParticipation participation : participations) {
            String value = answerRepository.findByParticipationAndQuestion(participation, question)
                .map(ResearchAnswer::getAnswerValue)
                .orElse("No answer");
            if ("MULTIPLE_CHOICE".equals(question.getQuestionType()) && value.contains(",")) {
                for (String part : value.split(",")) {
                    String trimmed = part.trim();
                    distribution.merge(trimmed, 1, Integer::sum);
                }
            } else {
                distribution.merge(value, 1, Integer::sum);
            }
        }
        return new ResearchQuestionAnalytics(
            question.getId(),
            question.getQuestionText(),
            question.getQuestionType(),
            distribution
        );
    }

    private ResearchGameCorrelation buildGameCorrelation(List<ResearchParticipation> participations) {
        List<Double> accuracies = new ArrayList<>();
        List<Double> reactionTimes = new ArrayList<>();
        List<Double> dPrimes = new ArrayList<>();
        int sessionsAnalyzed = 0;

        for (ResearchParticipation participation : participations) {
            if (PARTICIPATION_WITHDRAWN.equals(participation.getStatus())) {
                continue;
            }
            List<TestSession> sessions = testSessionRepository.findByUserIdOrderByStartTimeDesc(participation.getParticipant());
            for (TestSession session : sessions) {
                Optional<AggregatedMetrics> metricsOpt = aggregatedMetricsRepository.findBySessionId(session);
                if (metricsOpt.isEmpty()) {
                    continue;
                }
                AggregatedMetrics metrics = metricsOpt.get();
                accuracies.add(metrics.getAccuracy());
                reactionTimes.add(metrics.getAvgReactionTime());
                if (metrics.getDPrime() != null) {
                    dPrimes.add(metrics.getDPrime());
                }
                sessionsAnalyzed++;
            }
        }

        return new ResearchGameCorrelation(
            average(accuracies),
            average(reactionTimes),
            average(dPrimes),
            sessionsAnalyzed
        );
    }

    private List<ResearchTrendPoint> buildEnrollmentTrend(List<ResearchParticipation> participations) {
        Map<String, Integer> grouped = participations.stream()
            .filter(p -> p.getJoinedAt() != null)
            .collect(Collectors.groupingBy(
                p -> p.getJoinedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                LinkedHashMap::new,
                Collectors.summingInt(p -> 1)
            ));

        return grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new ResearchTrendPoint(entry.getKey(), entry.getValue()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ResearchParticipation> filterParticipationsForStudy(ResearchStudy study, List<ResearchFilterCriterion> filters) {
        return filterParticipations(study, filters);
    }

    @Transactional(readOnly = true)
    public List<User> filterParticipantsByCriteria(Long studyId, String email, List<ResearchFilterCriterion> filters) {
        ResearchStudy study = findOwnedStudy(studyId, email);
        return filterParticipations(study, filters).stream()
            .map(ResearchParticipation::getParticipant)
            .distinct()
            .toList();
    }

    private List<ResearchParticipation> filterParticipations(ResearchStudy study, List<ResearchFilterCriterion> filters) {
        List<ResearchParticipation> all = participationRepository.findByStudyOrderByJoinedAtDesc(study).stream()
            .filter(p -> !PARTICIPATION_WITHDRAWN.equals(p.getStatus()))
            .toList();

        if (filters == null || filters.isEmpty()) {
            return all;
        }

        return all.stream()
            .filter(participation -> matchesAllCriteria(participation, filters))
            .toList();
    }

    private boolean matchesAllCriteria(ResearchParticipation participation, List<ResearchFilterCriterion> filters) {
        Map<Long, String> answers = loadAnswerMap(participation);
        Map<String, String> answersByKey = loadAnswerKeyMap(participation);

        for (ResearchFilterCriterion filter : filters) {
            if (!matchesCriterion(participation, filter, answers, answersByKey)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCriterion(
        ResearchParticipation participation,
        ResearchFilterCriterion filter,
        Map<Long, String> answers,
        Map<String, String> answersByKey
    ) {
        String operator = filter.operator() == null ? "EQUALS" : filter.operator().toUpperCase(Locale.ROOT);

        if ("AGE_MIN".equals(operator) || "AGE_MAX".equals(operator) || "AGE_BETWEEN".equals(operator)) {
            int age = resolveAge(answersByKey.get(KEY_DOB));
            if ("AGE_MIN".equals(operator)) {
                return age >= Optional.ofNullable(filter.minAge()).orElse(0);
            }
            if ("AGE_MAX".equals(operator)) {
                return age <= Optional.ofNullable(filter.maxAge()).orElse(Integer.MAX_VALUE);
            }
            int min = Optional.ofNullable(filter.minAge()).orElse(0);
            int max = Optional.ofNullable(filter.maxAge()).orElse(Integer.MAX_VALUE);
            return age >= min && age <= max;
        }

        String answerValue;
        if (filter.questionKey() != null && !filter.questionKey().isBlank()) {
            answerValue = answersByKey.getOrDefault(filter.questionKey(), "");
        } else if (filter.questionId() != null) {
            answerValue = answers.getOrDefault(filter.questionId(), "");
        } else {
            return true;
        }

        return switch (operator) {
            case "NOT_EQUALS" -> !answerValue.equalsIgnoreCase(safeValue(filter));
            case "CONTAINS" -> answerValue.toLowerCase(Locale.ROOT).contains(safeValue(filter).toLowerCase(Locale.ROOT));
            case "IN" -> filter.values() != null && filter.values().stream()
                .anyMatch(value -> value.equalsIgnoreCase(answerValue));
            case "YES" -> isTruthy(answerValue);
            case "NO" -> !isTruthy(answerValue);
            default -> answerValue.equalsIgnoreCase(safeValue(filter));
        };
    }

    private int resolveAge(String dobValue) {
        if (dobValue == null || dobValue.isBlank()) {
            return -1;
        }
        try {
            LocalDate dob = LocalDate.parse(dobValue.substring(0, Math.min(10, dobValue.length())));
            return Period.between(dob, LocalDate.now()).getYears();
        } catch (DateTimeParseException ex) {
            return -1;
        }
    }

    private boolean isTruthy(String value) {
        return "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private String safeValue(ResearchFilterCriterion filter) {
        return filter.value() == null ? "" : filter.value();
    }

    private Map<Long, String> loadAnswerMap(ResearchParticipation participation) {
        return answerRepository.findByParticipation(participation).stream()
            .collect(Collectors.toMap(answer -> answer.getQuestion().getId(), ResearchAnswer::getAnswerValue));
    }

    private Map<String, String> loadAnswerKeyMap(ResearchParticipation participation) {
        return answerRepository.findByParticipation(participation).stream()
            .filter(answer -> answer.getQuestion().getQuestionKey() != null)
            .collect(Collectors.toMap(
                answer -> answer.getQuestion().getQuestionKey(),
                ResearchAnswer::getAnswerValue,
                (left, right) -> right
            ));
    }

    private void saveAnswers(ResearchParticipation participation, List<ResearchAnswerRequest> answers) {
        if (answers == null) {
            return;
        }

        List<ResearchQuestion> questions = questionRepository.findByStudyOrderBySortOrderAsc(participation.getStudy());
        Map<Long, ResearchQuestion> questionMap = questions.stream()
            .collect(Collectors.toMap(ResearchQuestion::getId, question -> question));

        for (ResearchAnswerRequest answerRequest : answers) {
            ResearchQuestion question = questionMap.get(answerRequest.questionId());
            if (question == null) {
                throw new BadRequestException("Invalid question in submission.");
            }

            ResearchAnswer answer = answerRepository.findByParticipationAndQuestion(participation, question)
                .orElseGet(() -> {
                    ResearchAnswer created = new ResearchAnswer();
                    created.setParticipation(participation);
                    created.setQuestion(question);
                    return created;
                });
            answer.setAnswerValue(answerRequest.value());
            answerRepository.save(answer);
        }

        validateRequiredAnswers(participation, questions);
    }

    private void validateRequiredAnswers(ResearchParticipation participation, List<ResearchQuestion> questions) {
        Map<Long, String> answers = loadAnswerMap(participation);
        for (ResearchQuestion question : questions) {
            if (!question.isRequired()) {
                continue;
            }
            String value = answers.get(question.getId());
            if (value == null || value.isBlank()) {
                throw new BadRequestException("Required question not answered: " + question.getQuestionText());
            }
        }
    }

    private void updateProgress(ResearchParticipation participation) {
        List<ResearchQuestion> questions = questionRepository.findByStudyOrderBySortOrderAsc(participation.getStudy());
        long requiredCount = questions.stream().filter(ResearchQuestion::isRequired).count();
        if (requiredCount == 0) {
            participation.setProgressPercent(100);
            participation.setStatus(PARTICIPATION_COMPLETED);
            participation.setCompletedAt(LocalDateTime.now());
            participationRepository.save(participation);
            return;
        }

        Map<Long, String> answers = loadAnswerMap(participation);
        long answeredRequired = questions.stream()
            .filter(ResearchQuestion::isRequired)
            .filter(question -> {
                String value = answers.get(question.getId());
                return value != null && !value.isBlank();
            })
            .count();

        int progress = (int) Math.round((answeredRequired * 100.0) / requiredCount);
        participation.setProgressPercent(progress);
        if (progress >= 100) {
            participation.setStatus(PARTICIPATION_COMPLETED);
            participation.setCompletedAt(LocalDateTime.now());
        } else if (progress > 0) {
            participation.setStatus(PARTICIPATION_IN_PROGRESS);
        }
        participationRepository.save(participation);
    }

    private void createDefaultDemographicQuestions(ResearchStudy study) {
        ResearchQuestion dob = new ResearchQuestion();
        dob.setStudy(study);
        dob.setQuestionText("Date of Birth");
        dob.setQuestionType("SHORT_TEXT");
        dob.setRequired(true);
        dob.setSortOrder(0);
        dob.setQuestionKey(KEY_DOB);
        dob.setDemographicDefault(true);
        questionRepository.save(dob);

        ResearchQuestion gender = new ResearchQuestion();
        gender.setStudy(study);
        gender.setQuestionText("Gender");
        gender.setQuestionType("DROPDOWN");
        gender.setOptionsJson(toOptionsJson(List.of("Female", "Male", "Non-binary", "Prefer not to say")));
        gender.setRequired(true);
        gender.setSortOrder(1);
        gender.setQuestionKey(KEY_GENDER);
        gender.setDemographicDefault(true);
        questionRepository.save(gender);
    }

    private void addCustomQuestions(ResearchStudy study, List<ResearchQuestionRequest> customQuestions, int startOrder) {
        if (customQuestions == null) {
            return;
        }

        int order = startOrder;
        for (ResearchQuestionRequest request : customQuestions) {
            validateQuestionRequest(request);
            ResearchQuestion question = buildQuestion(study, request, order++, null, false);
            questionRepository.save(question);
        }
    }

    private ResearchQuestion buildQuestion(
        ResearchStudy study,
        ResearchQuestionRequest request,
        int sortOrder,
        String questionKey,
        boolean demographicDefault
    ) {
        ResearchQuestion question = new ResearchQuestion();
        question.setStudy(study);
        question.setQuestionText(request.questionText().trim());
        question.setQuestionType(normalizeQuestionType(request.questionType()));
        question.setOptionsJson(toOptionsJson(request.options()));
        question.setRequired(request.required());
        question.setSortOrder(sortOrder);
        question.setQuestionKey(questionKey);
        question.setDemographicDefault(demographicDefault);
        return question;
    }

    private void applyStudyFields(ResearchStudy study, ResearchStudyRequest request) {
        study.setTitle(request.title().trim());
        study.setDescription(request.description().trim());
        study.setInstructions(request.instructions());
        study.setParticipationRequirements(request.participationRequirements());
        study.setEstimatedDuration(request.estimatedDuration());
        study.setResearchType(normalizeResearchType(request.researchType()));
        study.setRewarded(request.rewarded());
        study.setRewardDetails(request.rewarded() ? request.rewardDetails() : null);
        study.setAnonymousFriendly(request.anonymousFriendly());
        study.setConsentText(request.consentText() == null || request.consentText().isBlank()
            ? defaultConsentText()
            : request.consentText());
    }

    private void validateStudyRequest(ResearchStudyRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("Study title is required.");
        }
        if (request.description() == null || request.description().isBlank()) {
            throw new BadRequestException("Study description is required.");
        }
        if (request.researchType() == null || !ALLOWED_TYPES.contains(normalizeResearchType(request.researchType()))) {
            throw new BadRequestException("Research type must be ONLINE_TESTING or IN_PERSON_TESTING.");
        }
        if (request.rewarded() && (request.rewardDetails() == null || request.rewardDetails().isBlank())) {
            throw new BadRequestException("Reward details are required for rewarded studies.");
        }
    }

    private void validateQuestionRequest(ResearchQuestionRequest request) {
        if (request.questionText() == null || request.questionText().isBlank()) {
            throw new BadRequestException("Question text is required.");
        }
        if (request.questionType() == null || !ALLOWED_QUESTION_TYPES.contains(normalizeQuestionType(request.questionType()))) {
            throw new BadRequestException("Unsupported question type.");
        }
    }

    private void validateCohortRequest(ResearchCohortRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Cohort name is required.");
        }
        if (request.filters() == null || request.filters().isEmpty()) {
            throw new BadRequestException("At least one filter criterion is required.");
        }
    }

    private ResearchStudyResponse toStudyResponse(ResearchStudy study) {
        int participantCount = (int) participationRepository.countByStudyAndStatusNot(study, PARTICIPATION_WITHDRAWN);
        return new ResearchStudyResponse(
            study.getId(),
            study.getTitle(),
            study.getDescription(),
            study.getCreator().getEmail(),
            study.getResearchType(),
            study.isRewarded(),
            study.getRewardDetails(),
            study.getParticipationRequirements(),
            study.getEstimatedDuration(),
            study.isAnonymousFriendly(),
            study.getStatus(),
            participantCount,
            study.getCreatedAt()
        );
    }

    private ResearchStudyDetailResponse toStudyDetailResponse(ResearchStudy study) {
        List<ResearchQuestionResponse> questions = questionRepository.findByStudyOrderBySortOrderAsc(study).stream()
            .map(this::toQuestionResponse)
            .toList();

        int participantCount = (int) participationRepository.countByStudyAndStatusNot(study, PARTICIPATION_WITHDRAWN);

        return new ResearchStudyDetailResponse(
            study.getId(),
            study.getTitle(),
            study.getDescription(),
            study.getInstructions(),
            study.getCreator().getEmail(),
            study.getResearchType(),
            study.isRewarded(),
            study.getRewardDetails(),
            study.getParticipationRequirements(),
            study.getEstimatedDuration(),
            study.isAnonymousFriendly(),
            study.getConsentText(),
            study.getStatus(),
            participantCount,
            study.getCreatedAt(),
            questions
        );
    }

    private ResearchQuestionResponse toQuestionResponse(ResearchQuestion question) {
        return new ResearchQuestionResponse(
            question.getId(),
            question.getQuestionText(),
            question.getQuestionType(),
            parseOptions(question.getOptionsJson()),
            question.isRequired(),
            question.getSortOrder(),
            question.getQuestionKey(),
            question.isDemographicDefault()
        );
    }

    private ResearchParticipationResponse toParticipationResponse(ResearchParticipation participation) {
        List<ResearchAnswerRequest> answers = answerRepository.findByParticipation(participation).stream()
            .map(answer -> new ResearchAnswerRequest(answer.getQuestion().getId(), answer.getAnswerValue()))
            .toList();

        String displayName = participation.isAnonymous()
            ? participation.getAnonymousIdentifier()
            : participation.getParticipant().getEmail();

        return new ResearchParticipationResponse(
            participation.getId(),
            participation.getStudy().getId(),
            participation.getStudy().getTitle(),
            participation.getStudy().getCreator().getEmail(),
            participation.getStudy().getResearchType(),
            participation.getStudy().isRewarded(),
            participation.getStudy().getRewardDetails(),
            participation.isAnonymous(),
            displayName,
            participation.getStatus(),
            participation.getProgressPercent(),
            participation.getJoinedAt(),
            participation.getWithdrawnAt(),
            answers
        );
    }

    private ResearchParticipantSummary toParticipantSummary(ResearchParticipation participation) {
        String displayName = participation.isAnonymous()
            ? participation.getAnonymousIdentifier()
            : participation.getParticipant().getEmail();

        return new ResearchParticipantSummary(
            participation.getId(),
            displayName,
            participation.isAnonymous(),
            participation.getStatus(),
            participation.getProgressPercent(),
            participation.getJoinedAt()
        );
    }

    private ResearchCohortResponse toCohortResponse(ResearchCohort cohort) {
        List<ResearchFilterCriterion> filters = parseFilters(cohort.getFilterCriteriaJson());
        int matched = filterParticipations(cohort.getStudy(), filters).size();
        return new ResearchCohortResponse(
            cohort.getId(),
            cohort.getName(),
            cohort.getDescription(),
            filters,
            matched,
            cohort.getCreatedAt()
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

    private ResearchStudy findPublishedStudy(Long studyId) {
        ResearchStudy study = studyRepository.findById(studyId)
            .orElseThrow(() -> new BadRequestException("Study was not found."));
        if (!STATUS_PUBLISHED.equals(study.getStatus())) {
            throw new BadRequestException("Study is not available for participation.");
        }
        return study;
    }

    private ResearchStudy findOwnedStudy(Long studyId, String email) {
        ResearchStudy study = studyRepository.findById(studyId)
            .orElseThrow(() -> new BadRequestException("Study was not found."));
        if (!study.getCreator().getEmail().equals(email)) {
            throw new UnauthorizedException("You do not have access to this study.");
        }
        return study;
    }

    private ResearchParticipation findOwnedParticipation(Long participationId, String email) {
        ResearchParticipation participation = participationRepository.findById(participationId)
            .orElseThrow(() -> new BadRequestException("Participation was not found."));
        if (!participation.getParticipant().getEmail().equals(email)) {
            throw new UnauthorizedException("You do not have access to this participation.");
        }
        return participation;
    }

    private String normalizeResearchType(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeQuestionType(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String defaultConsentText() {
        return "I consent to participate in this research study and allow my responses to be used for research analytics.";
    }

    private String toOptionsJson(List<String> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Question options could not be saved.");
        }
    }

    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private String toFilterJson(List<ResearchFilterCriterion> filters) {
        try {
            return objectMapper.writeValueAsString(filters);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Filter criteria could not be saved.");
        }
    }

    private List<ResearchFilterCriterion> parseFilters(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ResearchFilterCriterion>>() {});
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Filter criteria could not be parsed.");
        }
    }

    private Double average(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
