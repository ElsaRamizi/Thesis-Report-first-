package com.example.demo.repository;

import com.example.demo.model.ResearchQuestion;
import com.example.demo.model.ResearchStudy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchQuestionRepository extends JpaRepository<ResearchQuestion, Long> {

    List<ResearchQuestion> findByStudyOrderBySortOrderAsc(ResearchStudy study);

    Optional<ResearchQuestion> findByStudyAndQuestionKey(ResearchStudy study, String questionKey);
}
