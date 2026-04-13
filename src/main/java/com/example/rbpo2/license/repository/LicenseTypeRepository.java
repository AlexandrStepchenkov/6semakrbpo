package com.example.rbpo2.license.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rbpo2.license.model.LicenseType;

public interface LicenseTypeRepository extends JpaRepository<LicenseType, UUID> {
}
