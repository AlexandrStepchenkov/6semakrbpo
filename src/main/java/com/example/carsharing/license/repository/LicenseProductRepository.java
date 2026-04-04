package com.example.carsharing.license.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.carsharing.license.model.LicenseProduct;

public interface LicenseProductRepository extends JpaRepository<LicenseProduct, UUID> {
}
