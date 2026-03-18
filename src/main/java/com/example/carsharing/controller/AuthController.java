package com.example.carsharing.controller;

import com.example.carsharing.model.AppUser;
import com.example.carsharing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user) {
        try {
            return ResponseEntity.ok(authService.register(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AppUser request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        return ResponseEntity.ok(authService.login(request.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody java.util.Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.refresh(body.get("refreshToken")));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}