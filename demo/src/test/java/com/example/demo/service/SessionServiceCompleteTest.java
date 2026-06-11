package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.SessionCompleteRequest;
import com.example.demo.dto.SessionResultResponse;
import com.example.demo.dto.TrialDataRequest;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import com.example.demo.model.User;
import com.example.demo.repository.AggregatedMetricsRepository;
import com.example.demo.repository.TestSessionRepository;
import com.example.demo.repository.TrialDataRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionServiceCompleteTest {

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

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(
            userRepository,
            testSessionRepository,
            trialDataRepository,
            aggregatedMetricsRepository,
            taskCatalogService
        );
    }

    @Test
    void completeSessionPersistsTrialsWithIndexes() {
        User user = new User("user@test.com", "pw", "USER");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        TestSession savedSession = new TestSession(
            user,
            "stroop",
            LocalDateTime.now().minusMinutes(2),
            LocalDateTime.now(),
            "Fixed",
            null,
            null
        );
        when(testSessionRepository.save(any(TestSession.class))).thenReturn(savedSession);
        when(trialDataRepository.save(any(TrialData.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aggregatedMetricsRepository.save(any(AggregatedMetrics.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(taskCatalogService.getTaskById("stroop")).thenReturn(
            new com.example.demo.dto.TaskDefinitionResponse(
                "stroop",
                "Stroop",
                "desc",
                4,
                "Fixed",
                List.of("accuracy")
            )
        );

        SessionCompleteRequest request = new SessionCompleteRequest();
        request.setTaskType("stroop");
        request.setDifficultyLevel("Fixed");
        request.setStartTime(LocalDateTime.now().minusMinutes(2));
        request.setEndTime(LocalDateTime.now());
        TrialDataRequest trial = new TrialDataRequest();
        trial.setStimulus("RED|red|congruent");
        trial.setResponse("Red");
        trial.setReactionTime(420L);
        trial.setCorrect(true);
        trial.setTimestamp(LocalDateTime.now());
        request.setTrials(List.of(trial));

        SessionResultResponse response = sessionService.completeSession("user@test.com", request);

        ArgumentCaptor<TrialData> captor = ArgumentCaptor.forClass(TrialData.class);
        verify(trialDataRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getTrialIndex());
        assertNotNull(response);
        assertEquals("stroop", response.getTaskType());
    }
}
