package com.example.rbpo2.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.rbpo2.model.AppUser;
import com.example.rbpo2.model.SessionStatus;
import com.example.rbpo2.model.UserSession;
import com.example.rbpo2.repository.AppUserRepository;
import com.example.rbpo2.repository.UserSessionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserSessionRepository sessionRepo;
    private final AppUserRepository appUserRepository;

    @Transactional
    public UserSession saveSession(String username, String refreshToken) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserSession session = new UserSession();

        session.setUser(appUser);

        session.setRefreshToken(refreshToken);
        session.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        session.setStatus(SessionStatus.ACTIVE);

        entityManager.persist(session);
        return session;
    }

    @Transactional
    public void revokeSession(String refreshToken) {
        sessionRepo.findByRefreshToken(refreshToken)
                .ifPresent(s -> {
                    s.setStatus(SessionStatus.REVOKED);
                    entityManager.merge(s);
                });
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return sessionRepo.findByRefreshToken(refreshToken)
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
            .filter(s -> s.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }
}