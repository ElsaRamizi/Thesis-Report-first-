package com.example.demo.repository;

import com.example.demo.model.AggregatedMetrics;
import com.example.demo.model.TestSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AggregatedMetricsRepository extends JpaRepository<AggregatedMetrics, Long> {
    Optional<AggregatedMetrics> findBySessionId(TestSession sessionId);
}
