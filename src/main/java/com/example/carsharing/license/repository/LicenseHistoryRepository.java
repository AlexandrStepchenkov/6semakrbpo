package com.example.carsharing.license.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.carsharing.license.model.LicenseHistory;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {
}
