package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.Clinician;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import com.example.demo.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EntityPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicianRepository clinicianRepository;

    @Autowired
    private TestSessionRepository testSessionRepository;

    @Autowired
    private TrialDataRepository trialDataRepository;

    @Autowired
    private AggregatedMetricsRepository aggregatedMetricsRepository;

    @Test
    void persistsFullEntityGraph() {
        User user = userRepository.save(new User("clinician@test.com", "secret", "CLINICIAN"));

        Clinician clinician = clinicianRepository.save(new Clinician(user, "Mind Metrics Lab"));

        TestSession session = testSessionRepository.save(
            new TestSession(
                user,
                "STROOP",
                LocalDateTime.of(2026, 3, 12, 10, 0),
                LocalDateTime.of(2026, 3, 12, 10, 5),
                "MEDIUM"
            )
        );

        TrialData trialData = trialDataRepository.save(
            new TrialData(
                session,
                "RED",
                "BLUE",
                450L,
                false,
                LocalDateTime.of(2026, 3, 12, 10, 1)
            )
        );

        AggregatedMetrics metrics = aggregatedMetricsRepository.save(
            new AggregatedMetrics(session, 450.0, 0.8, 0.2)
        );

        assertNotNull(clinician.getId());
        assertNotNull(session.getId());
        assertNotNull(trialData.getId());
        assertNotNull(metrics.getId());

        TestSession savedSession = testSessionRepository.findById(session.getId()).orElseThrow();
        TrialData savedTrialData = trialDataRepository.findById(trialData.getId()).orElseThrow();
        AggregatedMetrics savedMetrics = aggregatedMetricsRepository.findById(metrics.getId()).orElseThrow();

        assertEquals(user.getId(), clinician.getUserId().getId());
        assertEquals(user.getId(), savedSession.getUserId().getId());
        assertEquals(savedSession.getId(), savedTrialData.getSessionId().getId());
        assertEquals(savedSession.getId(), savedMetrics.getSessionId().getId());
        assertEquals("Mind Metrics Lab", clinician.getOrganization());
        assertEquals("STROOP", savedSession.getTaskType());
        assertEquals(450L, savedTrialData.getReactionTime());
        assertFalse(savedTrialData.isCorrect());
        assertEquals(450.0, savedMetrics.getAvgReactionTime());
        assertEquals(0.8, savedMetrics.getAccuracy());
        assertEquals(0.2, savedMetrics.getErrorRate());
    }

    @Test
    void rejectsClinicianWithoutOrganization() {
        User user = userRepository.save(new User("missing-org@test.com", "secret", "CLINICIAN"));
        Clinician clinician = new Clinician(user, null);

        assertThrows(DataIntegrityViolationException.class, () -> clinicianRepository.saveAndFlush(clinician));
    }

    @Test
    void rejectsTrialDataWithoutSession() {
        TrialData trialData = new TrialData(
            null,
            "GREEN",
            "GREEN",
            300L,
            true,
            LocalDateTime.of(2026, 3, 12, 11, 0)
        );

        assertThrows(DataIntegrityViolationException.class, () -> trialDataRepository.saveAndFlush(trialData));
    }
}
