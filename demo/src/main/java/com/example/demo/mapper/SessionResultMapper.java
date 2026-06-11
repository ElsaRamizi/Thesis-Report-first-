package com.example.demo.mapper;

import com.example.demo.dto.SessionResultResponse;
import com.example.demo.dto.TrialResultResponse;
import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import java.util.List;
import java.util.function.Function;

public final class SessionResultMapper {

    private SessionResultMapper() {}

    public static SessionResultResponse toResponse(
        TestSession session,
        AggregatedMetrics metrics,
        List<TrialData> trials,
        Function<String, String> titleResolver,
        Function<TrialData, TrialResultResponse> trialMapper
    ) {
        List<TrialResultResponse> trialResponses = trials.stream().map(trialMapper).toList();
        SessionResultResponse response = new SessionResultResponse(
            session.getId(),
            session.getTaskType(),
            titleResolver.apply(session.getTaskType()),
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
        response.setMedianReactionTime(metrics.getMedianReactionTime());
        response.setMissRate(metrics.getMissRate());
        response.setResponseVariability(metrics.getResponseVariability());
        response.setMaxSpanReached(metrics.getMaxSpanReached());
        response.setStroopInterferenceMs(metrics.getStroopInterferenceMs());
        response.setStroopCongruentAccuracy(metrics.getStroopCongruentAccuracy());
        response.setStroopIncongruentAccuracy(metrics.getStroopIncongruentAccuracy());
        return response;
    }
}
