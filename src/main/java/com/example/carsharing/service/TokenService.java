package com.example.carsharing.service;

import com.example.carsharing.model.AppUser;
import com.example.carsharing.model.SessionStatus;
import com.example.carsharing.model.UserSession;
import com.example.carsharing.repository.UserSessionRepository;
import com.example.carsharing.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserSessionRepository sessionRepo;
    private final AppUserRepository appUserRepository;

    @Transactional
    public UserSession saveSession(String username, String refreshToken) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserSession session = new UserSession();

        session.setUser(appUser);

        session.setRefreshToken(refreshToken);
        session.setExpiresAt(java.time.Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS));
        session.setStatus(SessionStatus.ACTIVE);

        return sessionRepo.save(session);
    }

    @Transactional
    public void revokeSession(String refreshToken) {
        sessionRepo.findByRefreshToken(refreshToken)
                .ifPresent(s -> {
                    s.setStatus(SessionStatus.REVOKED);
                    sessionRepo.save(s);
                });
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return sessionRepo.findByRefreshToken(refreshToken)
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .filter(s -> s.getExpiresAt().isAfter(java.time.Instant.now()))
                .isPresent();
    }
}