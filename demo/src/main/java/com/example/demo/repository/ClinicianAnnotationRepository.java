package com.example.demo.repository;

import com.example.demo.model.ClinicianAnnotation;
import com.example.demo.model.TestSession;
import com.example.demo.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicianAnnotationRepository extends JpaRepository<ClinicianAnnotation, Long> {

    List<ClinicianAnnotation> findByParticipantOrderByCreatedAtDesc(User participant);

    List<ClinicianAnnotation> findBySessionOrderByCreatedAtDesc(TestSession session);
}
