package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.dto.AutomatedReportResponse;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import com.example.demo.model.User;
import com.example.demo.repository.AggregatedMetricsRepository;
import com.example.demo.repository.ParticipantProfileRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.TrialDataRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClinicianServiceReportTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TestSessionRepository testSessionRepository;
    @Mock
    private TrialDataRepository trialDataRepository;
    @Mock
    private AggregatedMetricsRepository aggregatedMetricsRepository;
    @Mock
    private TaskCatalogService taskCatalogService;
    @Mock
    private ClinicianAccessService clinicianAccessService;
    @Mock
    private ParticipantProfileRepository participantProfileRepository;
    @Mock
    private SessionService sessionService;

    private ClinicianService clinicianService;

    @BeforeEach
    void setUp() {
        clinicianService = new ClinicianService(
            userRepository,
            testSessionRepository,
            trialDataRepository,
            aggregatedMetricsRepository,
            taskCatalogService,
            clinicianAccessService,
            participantProfileRepository,
            sessionService
        );
    }

    @Test
    void generateAutomatedReportFlagsAlertOnRecentAccuracyDrop() {
        User participant = new User("patient@test.com", "pw", "USER");
        LocalDateTime day10 = LocalDateTime.now().minusDays(10);
        LocalDateTime day5 = LocalDateTime.now().minusDays(5);
        LocalDateTime today = LocalDateTime.now();

        TestSession session1 = buildSession(participant, day10);
        TestSession session2 = buildSession(participant, day5);
        TestSession session3 = buildSession(participant, today);

        when(clinicianAccessService.requireAccessibleParticipant("clinician@test.com", 5L)).thenReturn(participant);
        when(testSessionRepository.findByUserIdOrderByStartTimeDesc(participant))
            .thenReturn(List.of(session3, session2, session1));
        when(aggregatedMetricsRepository.findBySessionId(any(TestSession.class)))
            .thenAnswer(invocation -> {
                TestSession session = invocation.getArgument(0);
                if (session.getStartTime().equals(day10)) {
                    return Optional.of(metrics(session, 85.0, 500.0));
                }
                if (session.getStartTime().equals(day5)) {
                    return Optional.of(metrics(session, 80.0, 520.0));
                }
                return Optional.of(metrics(session, 60.0, 540.0));
            });

        AutomatedReportResponse report = clinicianService.generateAutomatedReport("clinician@test.com", 5L);

        assertEquals("ALERT", report.severity());
        assertTrue(report.findings().stream().anyMatch(finding -> finding.contains("Last 3 sessions: accuracy down")));
    }

    private TestSession buildSession(User user, LocalDateTime startTime) {
        return new TestSession(user, "stroop", startTime, startTime.plusMinutes(5), "Fixed", null, null);
    }

    private AggregatedMetrics metrics(TestSession session, double accuracy, double rt) {
        AggregatedMetrics metrics = new AggregatedMetrics(session, rt, accuracy, 100.0 - accuracy);
        metrics.setMissRate(0.05);
        metrics.setFalseAlarmRate(0.1);
        return metrics;
    }
}
