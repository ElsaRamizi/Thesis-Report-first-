package com.example.demo.repository;

import com.example.demo.model.TestSession;
import com.example.demo.model.TrialData;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrialDataRepository extends JpaRepository<TrialData, Long> {
    List<TrialData> findBySessionIdOrderByTimestampAsc(TestSession sessionId);
}
