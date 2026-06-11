package com.example.demo.repository;

import com.example.demo.model.ResearchParticipation;
import com.example.demo.model.ResearchStudy;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchParticipationRepository extends JpaRepository<ResearchParticipation, Long> {

    List<ResearchParticipation> findByParticipantOrderByJoinedAtDesc(User participant);

    List<ResearchParticipation> findByStudyOrderByJoinedAtDesc(ResearchStudy study);

    Optional<ResearchParticipation> findByStudyAndParticipant(ResearchStudy study, User participant);

    long countByStudyAndStatusNot(ResearchStudy study, String status);
}
