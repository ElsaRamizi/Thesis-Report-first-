package com.example.demo.repository;

import com.example.demo.model.ParticipantProfile;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantProfileRepository extends JpaRepository<ParticipantProfile, Long> {

    Optional<ParticipantProfile> findByUser(User user);

    List<ParticipantProfile> findByAssignedClinician(User assignedClinician);
}
