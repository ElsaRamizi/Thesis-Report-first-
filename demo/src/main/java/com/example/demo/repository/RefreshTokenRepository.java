package com.example.demo.repository;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    void deleteByUser(User user);
}
