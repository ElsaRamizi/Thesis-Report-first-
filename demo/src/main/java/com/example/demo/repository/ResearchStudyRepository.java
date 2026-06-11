package com.example.demo.repository;

import com.example.demo.model.ResearchStudy;
import com.example.demo.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchStudyRepository extends JpaRepository<ResearchStudy, Long> {

    List<ResearchStudy> findByCreatorOrderByCreatedAtDesc(User creator);

    List<ResearchStudy> findByStatusOrderByCreatedAtDesc(String status);
}
