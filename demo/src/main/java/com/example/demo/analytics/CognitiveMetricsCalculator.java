package com.example.demo.analytics;

import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CognitiveMetricsCalculator {

    private static final String HIT = "HIT";
    private static final String CORRECT_REJECTION = "CORRECT_REJECTION";
    private static final String FALSE_ALARM = "FALSE_ALARM";
    private static final String MISS = "MISS";

    private CognitiveMetricsCalculator() {}

    public static ExtendedSessionMetrics computeExtendedMetrics(TestSession session, List<TrialData> trials) {
        List<Long> reactionTimes = collectReactionTimes(trials);
        double medianReactionTime = median(reactionTimes);
        double responseVariability = standardDeviation(reactionTimes);
        MissRateResult missRateResult = computeMissRate(trials);

        return new ExtendedSessionMetrics(
            round(medianReactionTime),
            round(missRateResult.rate()),
            round(responseVariability)
        );
    }

    public static CognitiveMetricSnapshot snapshotFromMetrics(AggregatedMetrics metrics) {
        return new CognitiveMetricSnapshot(
            metrics.getAvgReactionTime(),
            metrics.getMedianReactionTime(),
            metrics.getAccuracy(),
            metrics.getErrorRate(),
            metrics.getFalseAlarmRate(),
            metrics.getMissRate(),
            metrics.getMaxNReached() == null ? null : metrics.getMaxNReached().doubleValue(),
            null,
            metrics.getResponseVariability()
        );
    }

    public static CognitiveMetricSnapshot averageSnapshots(List<CognitiveMetricSnapshot> snapshots) {
        return new CognitiveMetricSnapshot(
            average(snapshots.stream().map(CognitiveMetricSnapshot::avgReactionTime).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::medianReactionTime).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::accuracy).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::errorRate).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::falseAlarmRate).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::missRate).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::maxNReached).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::improvementRate).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::responseVariability).toList())
        );
    }

    public static Double computeImprovementRate(List<CognitiveMetricSnapshot> chronologicalSnapshots) {
        if (chronologicalSnapshots.size() < 2) {
            return null;
        }
        CognitiveMetricSnapshot first = chronologicalSnapshots.get(0);
        CognitiveMetricSnapshot latest = chronologicalSnapshots.get(chronologicalSnapshots.size() - 1);
        if (first.accuracy() == null || latest.accuracy() == null || first.accuracy() == 0) {
            return null;
        }
        return round(((latest.accuracy() - first.accuracy()) / first.accuracy()) * 100.0);
    }

    public static List<CognitiveTimelinePoint> buildRollingAverage(List<CognitiveTimelinePoint> points, int windowSize) {
        if (points.isEmpty()) {
            return List.of();
        }
        List<CognitiveTimelinePoint> rolling = new ArrayList<>();
        for (int index = 0; index < points.size(); index++) {
            int start = Math.max(0, index - windowSize + 1);
            List<CognitiveTimelinePoint> window = points.subList(start, index + 1);
            rolling.add(new CognitiveTimelinePoint(
                points.get(index).sessionId(),
                points.get(index).label(),
                points.get(index).taskType(),
                points.get(index).startTime(),
                average(window.stream().map(CognitiveTimelinePoint::avgReactionTime).toList()),
                average(window.stream().map(CognitiveTimelinePoint::accuracy).toList()),
                average(window.stream().map(CognitiveTimelinePoint::errorRate).toList()),
                average(window.stream().map(CognitiveTimelinePoint::missRate).toList()),
                true
            ));
        }
        return rolling;
    }

    public static CognitiveProfile buildProfile(CognitiveMetricSnapshot snapshot) {
        double memory = normalize(snapshot.maxNReached(), 1, 5);
        double reactionSpeed = inverseNormalize(snapshot.avgReactionTime(), 200, 1200);
        double attention = normalize(snapshot.accuracy(), 40, 100);
        double consistency = inverseNormalize(snapshot.responseVariability(), 20, 250);
        double inhibition = inverseNormalize(
            snapshot.falseAlarmRate() == null ? null : snapshot.falseAlarmRate() * 100,
            5,
            40
        );
        double adaptability = normalize(snapshot.improvementRate(), -10, 25);

        return new CognitiveProfile(
            round(memory),
            round(reactionSpeed),
            round(attention),
            round(consistency),
            round(inhibition),
            round(adaptability)
        );
    }

    public static SessionAnalysis analyzeSession(List<TrialData> trials) {
        List<Long> reactionTimes = collectReactionTimes(trials);
        long incorrectResponses = trials.stream().filter(trial -> !trial.isCorrect()).count();
        Double fatigueIndicator = computeFatigueIndicator(reactionTimes);
        List<String> anomalies = detectAnomalies(reactionTimes);

        return new SessionAnalysis(
            trials.size(),
            incorrectResponses,
            distributionBuckets(reactionTimes),
            fatigueIndicator,
            anomalies
        );
    }

    public static MetricComparison compareToBaseline(CognitiveMetricSnapshot current, CognitiveMetricSnapshot baseline) {
        return new MetricComparison(
            percentDelta(current.avgReactionTime(), baseline.avgReactionTime(), true),
            percentDelta(current.accuracy(), baseline.accuracy(), false),
            percentDelta(current.errorRate(), baseline.errorRate(), true),
            percentDelta(current.missRate(), baseline.missRate(), true)
        );
    }

    public static CohortStatistics cohortStatistics(List<CognitiveMetricSnapshot> snapshots) {
        return new CohortStatistics(
            snapshots.size(),
            average(snapshots.stream().map(CognitiveMetricSnapshot::avgReactionTime).toList()),
            medianOfDoubles(snapshots.stream().map(CognitiveMetricSnapshot::avgReactionTime).filter(Objects::nonNull).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::accuracy).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::errorRate).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::falseAlarmRate).toList()),
            average(snapshots.stream().map(CognitiveMetricSnapshot::maxNReached).toList()),
            standardDeviationOfDoubles(snapshots.stream().map(CognitiveMetricSnapshot::avgReactionTime).filter(Objects::nonNull).toList()),
            varianceOfDoubles(snapshots.stream().map(CognitiveMetricSnapshot::accuracy).filter(Objects::nonNull).toList())
        );
    }

    private static MissRateResult computeMissRate(List<TrialData> trials) {
        long misses = 0;
        long matchOpportunities = 0;

        for (TrialData trial : trials) {
            if (trial.getPositionOutcome() != null || trial.getLetterOutcome() != null) {
                if (Boolean.TRUE.equals(trial.getExpectedPositionMatch())) {
                    matchOpportunities++;
                    misses += MISS.equals(trial.getPositionOutcome()) ? 1 : 0;
                }
                if (Boolean.TRUE.equals(trial.getExpectedLetterMatch())) {
                    matchOpportunities++;
                    misses += MISS.equals(trial.getLetterOutcome()) ? 1 : 0;
                }
            } else if (!trial.isCorrect()) {
                misses++;
                matchOpportunities++;
            }
        }

        double rate = matchOpportunities == 0 ? 0 : (misses * 100.0) / matchOpportunities;
        return new MissRateResult(rate, misses, matchOpportunities);
    }

    private static List<Long> collectReactionTimes(List<TrialData> trials) {
        List<Long> values = new ArrayList<>();
        for (TrialData trial : trials) {
            if (trial.getReactionTimePosition() != null) {
                values.add(trial.getReactionTimePosition());
            }
            if (trial.getReactionTimeLetter() != null) {
                values.add(trial.getReactionTimeLetter());
            }
            if (trial.getReactionTimePosition() == null && trial.getReactionTimeLetter() == null && trial.getReactionTime() != null) {
                values.add(trial.getReactionTime());
            }
        }
        return values;
    }

    private static double median(List<Long> values) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
        }
        return sorted.get(middle);
    }

    private static double medianOfDoubles(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
        }
        return sorted.get(middle);
    }

    private static double standardDeviation(List<Long> values) {
        if (values.size() < 2) {
            return 0;
        }
        double mean = values.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = values.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance);
    }

    private static double standardDeviationOfDoubles(List<Double> values) {
        if (values.size() < 2) {
            return 0;
        }
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance);
    }

    private static double varianceOfDoubles(List<Double> values) {
        if (values.size() < 2) {
            return 0;
        }
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return values.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0);
    }

    private static Double computeFatigueIndicator(List<Long> reactionTimes) {
        if (reactionTimes.size() < 4) {
            return null;
        }
        int split = reactionTimes.size() / 2;
        double firstHalf = reactionTimes.subList(0, split).stream().mapToLong(Long::longValue).average().orElse(0);
        double secondHalf = reactionTimes.subList(split, reactionTimes.size()).stream().mapToLong(Long::longValue).average().orElse(0);
        if (firstHalf == 0) {
            return null;
        }
        return round(((secondHalf - firstHalf) / firstHalf) * 100.0);
    }

    private static List<String> detectAnomalies(List<Long> reactionTimes) {
        if (reactionTimes.size() < 5) {
            return List.of();
        }
        double mean = reactionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double stdDev = standardDeviation(reactionTimes);
        if (stdDev == 0) {
            return List.of();
        }
        List<String> anomalies = new ArrayList<>();
        for (int index = 0; index < reactionTimes.size(); index++) {
            double zScore = Math.abs((reactionTimes.get(index) - mean) / stdDev);
            if (zScore >= 2.5) {
                anomalies.add("Trial " + (index + 1) + " reaction time deviates significantly (" + Math.round(zScore * 10) / 10.0 + "σ)");
            }
        }
        return anomalies;
    }

    private static List<DistributionBucket> distributionBuckets(List<Long> reactionTimes) {
        if (reactionTimes.isEmpty()) {
            return List.of();
        }
        long max = reactionTimes.stream().max(Comparator.naturalOrder()).orElse(1L);
        long bucketSize = Math.max(100, max / 5);
        List<DistributionBucket> buckets = new ArrayList<>();
        for (int bucketIndex = 0; bucketIndex < 5; bucketIndex++) {
            long lower = bucketIndex * bucketSize;
            long upper = lower + bucketSize;
            final int currentBucket = bucketIndex;
            long count = reactionTimes.stream()
                .filter(value -> value >= lower && (currentBucket == 4 || value < upper))
                .count();
            buckets.add(new DistributionBucket(lower + "-" + upper + " ms", count));
        }
        return buckets;
    }

    private static Double percentDelta(Double current, Double baseline, boolean lowerIsBetter) {
        if (current == null || baseline == null || baseline == 0) {
            return null;
        }
        double delta = ((current - baseline) / baseline) * 100.0;
        if (lowerIsBetter) {
            delta = -delta;
        }
        return round(delta);
    }

    private static Double average(List<Double> values) {
        List<Double> filtered = values.stream().filter(Objects::nonNull).toList();
        if (filtered.isEmpty()) {
            return null;
        }
        return round(filtered.stream().mapToDouble(Double::doubleValue).average().orElse(0));
    }

    private static double normalize(Double value, double min, double max) {
        if (value == null) {
            return 50;
        }
        double clamped = Math.max(min, Math.min(max, value));
        return ((clamped - min) / (max - min)) * 100.0;
    }

    private static double inverseNormalize(Double value, double min, double max) {
        if (value == null) {
            return 50;
        }
        return 100.0 - normalize(value, min, max);
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public record ExtendedSessionMetrics(
        Double medianReactionTime,
        Double missRate,
        Double responseVariability
    ) {}

    public record MissRateResult(double rate, long misses, long opportunities) {}

    public record CognitiveMetricSnapshot(
        Double avgReactionTime,
        Double medianReactionTime,
        Double accuracy,
        Double errorRate,
        Double falseAlarmRate,
        Double missRate,
        Double maxNReached,
        Double improvementRate,
        Double responseVariability
    ) {}

    public record CognitiveTimelinePoint(
        Long sessionId,
        String label,
        String taskType,
        java.time.LocalDateTime startTime,
        Double avgReactionTime,
        Double accuracy,
        Double errorRate,
        Double missRate,
        boolean rolling
    ) {}

    public record CognitiveProfile(
        Double memory,
        Double reactionSpeed,
        Double attention,
        Double consistency,
        Double inhibitionControl,
        Double adaptability
    ) {}

    public record SessionAnalysis(
        int totalTrials,
        long incorrectResponses,
        List<DistributionBucket> reactionTimeDistribution,
        Double fatigueIndicatorPercent,
        List<String> anomalies
    ) {}

    public record DistributionBucket(String label, long count) {}

    public record MetricComparison(
        Double reactionTimeDeltaPercent,
        Double accuracyDeltaPercent,
        Double errorRateDeltaPercent,
        Double missRateDeltaPercent
    ) {}

    public record CohortStatistics(
        int participantCount,
        Double avgReactionTime,
        Double medianReactionTime,
        Double avgAccuracy,
        Double avgErrorRate,
        Double avgFalseAlarmRate,
        Double avgMaxNReached,
        Double reactionTimeStdDev,
        Double accuracyVariance
    ) {}
}
