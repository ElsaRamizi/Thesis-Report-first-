package com.example.demo.repository;

import com.example.demo.model.Clinician;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicianRepository extends JpaRepository<Clinician, Long> {
}
