package com.example.demo.repository;

import com.example.demo.model.ResearchAnswer;
import com.example.demo.model.ResearchParticipation;
import com.example.demo.model.ResearchQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResearchAnswerRepository extends JpaRepository<ResearchAnswer, Long> {

    List<ResearchAnswer> findByParticipation(ResearchParticipation participation);

    Optional<ResearchAnswer> findByParticipationAndQuestion(ResearchParticipation participation, ResearchQuestion question);
}
