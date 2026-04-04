package com.example.carsharing.license.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.carsharing.license.model.DeviceLicense;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
    long countByLicense_Id(UUID licenseId);
    boolean existsByLicense_IdAndDevice_Id(UUID licenseId, UUID deviceId);
}
