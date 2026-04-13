package com.example.rbpo2.license.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rbpo2.license.model.LicenseDevice;

public interface LicenseDeviceRepository extends JpaRepository<LicenseDevice, UUID> {
    Optional<LicenseDevice> findByMacAddress(String macAddress);
}
