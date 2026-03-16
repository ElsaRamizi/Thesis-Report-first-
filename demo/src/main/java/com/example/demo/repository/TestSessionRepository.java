package com.example.demo.repository;

import com.example.demo.model.TestSession;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    Optional<TestSession> findTopByUserIdOrderByStartTimeDesc(User userId);
    List<TestSession> findByUserIdOrderByStartTimeDesc(User userId);
}
