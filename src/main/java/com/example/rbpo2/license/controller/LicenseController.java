package com.example.rbpo2.license.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.rbpo2.license.dto.ActivateLicenseRequest;
import com.example.rbpo2.license.dto.CheckLicenseRequest;
import com.example.rbpo2.license.dto.CreateLicenseRequest;
import com.example.rbpo2.license.dto.LicenseHistoryResponse;
import com.example.rbpo2.license.dto.LicenseResponse;
import com.example.rbpo2.license.dto.RenewLicenseRequest;
import com.example.rbpo2.license.dto.TicketResponse;
import com.example.rbpo2.license.service.LicenseService;
import com.example.rbpo2.model.AppUser;
import com.example.rbpo2.repository.AppUserRepository;

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LicenseResponse>> getAllLicenses() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser admin = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.getAllLicenses(admin.getId()));
    }

    @GetMapping("/by-code/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> getLicenseByCode(@PathVariable String code) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser admin = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.getLicenseByCode(code, admin.getId()));
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

    @GetMapping("/{licenseId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LicenseHistoryResponse>> getLicenseHistory(@PathVariable UUID licenseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return ResponseEntity.ok(licenseService.getLicenseHistory(licenseId, user.getId()));
    }
}
