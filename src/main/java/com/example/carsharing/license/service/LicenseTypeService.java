package com.example.carsharing.license.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.carsharing.license.model.LicenseType;
import com.example.carsharing.license.repository.LicenseTypeRepository;

@Service
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    public LicenseType getTypeOrFail(UUID typeId) {
        return licenseTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found"));
    }
}
