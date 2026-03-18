package com.example.carsharing.service;

import com.example.carsharing.config.JwtTokenUtils;
import com.example.carsharing.model.AppUser;
import com.example.carsharing.model.SessionStatus;
import com.example.carsharing.model.UserSession;
import com.example.carsharing.repository.AppUserRepository;
import com.example.carsharing.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepo;
    private final UserSessionRepository sessionRepo;
    private final JwtTokenUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh.expiration.days}")
    private long refreshDays;

    public record AuthResponse(String accessToken, String refreshToken) {}

    public AuthResponse register(AppUser user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) user.setRole("ROLE_USER");

        userRepo.save(user);
        return generateTokens(user);
    }

    public AuthResponse login(String username) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        return generateTokens(user);
    }

    public AuthResponse refresh(String oldToken) {
        UserSession session = sessionRepo.findByRefreshToken(oldToken)
                .orElseThrow(() -> new NoSuchElementException("Session not found"));

        if (session.getStatus() != SessionStatus.ACTIVE || session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(SessionStatus.REVOKED); // Или EXPIRED
            sessionRepo.save(session);
            throw new SecurityException("Token invalid");
        }

        session.setStatus(SessionStatus.REVOKED);
        sessionRepo.save(session);

        return generateTokens(session.getUser());
    }

    private AuthResponse generateTokens(AppUser user) {
        String access = jwtUtils.createAccessToken(user);
        String refresh = jwtUtils.createRefreshToken(user);

        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refresh)
                .status(SessionStatus.ACTIVE)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(refreshDays)))
                .build();
        sessionRepo.save(session);

        return new AuthResponse(access, refresh);
    }
}