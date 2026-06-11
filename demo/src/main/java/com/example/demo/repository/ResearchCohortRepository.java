package com.example.demo.repository;

import com.example.demo.model.ResearchCohort;
import com.example.demo.model.ResearchStudy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchCohortRepository extends JpaRepository<ResearchCohort, Long> {

    List<ResearchCohort> findByStudyOrderByCreatedAtDesc(ResearchStudy study);
}
