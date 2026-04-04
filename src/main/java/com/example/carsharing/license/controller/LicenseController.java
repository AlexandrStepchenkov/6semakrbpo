package com.example.carsharing.license.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.carsharing.license.dto.ActivateLicenseRequest;
import com.example.carsharing.license.dto.CheckLicenseRequest;
import com.example.carsharing.license.dto.CreateLicenseRequest;
import com.example.carsharing.license.dto.LicenseResponse;
import com.example.carsharing.license.dto.RenewLicenseRequest;
import com.example.carsharing.license.dto.TicketResponse;
import com.example.carsharing.license.service.LicenseService;
import com.example.carsharing.model.AppUser;
import com.example.carsharing.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final AppUserRepository appUserRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> createLicense(@RequestBody CreateLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser admin = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.createLicense(request, admin.getId()));
    }

    @PostMapping("/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> activateLicense(@RequestBody ActivateLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.activateLicense(request, user.getId()));
    }

    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> checkLicense(@RequestBody CheckLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.checkLicense(request, user.getId()));
    }

    @PostMapping("/renew")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> renewLicense(@RequestBody RenewLicenseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.renewLicense(request, user.getId()));
    }
}
