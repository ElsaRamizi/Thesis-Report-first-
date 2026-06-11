package com.example.demo.repository;

import com.example.demo.model.DoctorConnection;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorConnectionRepository extends JpaRepository<DoctorConnection, Long> {

    List<DoctorConnection> findByPatientOrderByCreatedAtDesc(User patient);

    List<DoctorConnection> findByDoctorEmailIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(String doctorEmail);

    Optional<DoctorConnection> findByIdAndPatient(Long id, User patient);
}
