package com.example.carsharing.license.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.carsharing.license.dto.LicenseTypeRequest;
import com.example.carsharing.license.model.LicenseType;
import com.example.carsharing.license.repository.LicenseTypeRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/license-types")
@RequiredArgsConstructor
public class LicenseTypeController {

    private final LicenseTypeRepository licenseTypeRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseType> createType(@RequestBody LicenseTypeRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getDefaultDurationInDays() == null || request.getDefaultDurationInDays() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        LicenseType type = new LicenseType();
        type.setName(request.getName());
        type.setDefaultDurationInDays(request.getDefaultDurationInDays());
        type.setDescription(request.getDescription());

        return ResponseEntity.status(HttpStatus.CREATED).body(licenseTypeRepository.save(type));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LicenseType>> getAllTypes() {
        return ResponseEntity.ok(licenseTypeRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LicenseType> getTypeById(@PathVariable UUID id) {
        Optional<LicenseType> type = licenseTypeRepository.findById(id);
        return type.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseType> updateType(@PathVariable UUID id, @RequestBody LicenseTypeRequest request) {
        Optional<LicenseType> typeOpt = licenseTypeRepository.findById(id);

        if (typeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (request.getDefaultDurationInDays() != null && request.getDefaultDurationInDays() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        LicenseType type = typeOpt.get();
        if (request.getName() != null) {
            type.setName(request.getName());
        }
        if (request.getDefaultDurationInDays() != null) {
            type.setDefaultDurationInDays(request.getDefaultDurationInDays());
        }
        if (request.getDescription() != null) {
            type.setDescription(request.getDescription());
        }

        return ResponseEntity.ok(licenseTypeRepository.save(type));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteType(@PathVariable UUID id) {
        if (!licenseTypeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        licenseTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
