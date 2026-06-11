package com.example.demo.analytics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.analytics.CognitiveMetricsCalculator.CognitiveMetricSnapshot;
import com.example.demo.analytics.CognitiveMetricsCalculator.MetricComparison;
import org.junit.jupiter.api.Test;

class CognitiveMetricsCalculatorTest {

    @Test
    void compareToBaselineCalculatesExpectedDeltas() {
        CognitiveMetricSnapshot baseline = new CognitiveMetricSnapshot(
            500.0, 480.0, 80.0, 20.0, 0.1, 0.05, 2.0, null, 40.0
        );
        CognitiveMetricSnapshot current = new CognitiveMetricSnapshot(
            400.0, 390.0, 90.0, 10.0, 0.05, 0.02, 3.0, null, 30.0
        );

        MetricComparison comparison = CognitiveMetricsCalculator.compareToBaseline(current, baseline);

        assertNotNull(comparison.reactionTimeDeltaPercent());
        assertNotNull(comparison.accuracyDeltaPercent());
        assertTrue(comparison.accuracyDeltaPercent() > 0);
        assertTrue(comparison.reactionTimeDeltaPercent() > 0);
    }
}
