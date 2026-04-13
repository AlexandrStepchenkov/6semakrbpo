package com.example.rbpo2.license.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rbpo2.license.model.LicenseHistory;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {

	List<LicenseHistory> findByLicense_IdOrderByChangeDateDescIdDesc(UUID licenseId);
}
