package com.example.carsharing.license.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.carsharing.license.model.LicenseProduct;
import com.example.carsharing.license.repository.LicenseProductRepository;

@Service
public class LicenseProductService {

    private final LicenseProductRepository licenseProductRepository;

    public LicenseProductService(LicenseProductRepository licenseProductRepository) {
        this.licenseProductRepository = licenseProductRepository;
    }

    public LicenseProduct getProductOrFail(UUID productId) {
        return licenseProductRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }
}
