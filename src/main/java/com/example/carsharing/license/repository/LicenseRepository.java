package com.example.carsharing.license.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.carsharing.license.model.License;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    Optional<License> findByCode(String code);

    @Query("""
            select l
            from License l
            join DeviceLicense dl on dl.license = l
            join dl.device d
            where d.id = :deviceId
              and l.user.id = :userId
              and l.product.id = :productId
              and l.blocked = false
              and l.endingDate >= :today
        """)
    Optional<License> findActiveByDeviceUserAndProduct(
            @Param("deviceId") UUID deviceId,
            @Param("userId") Long userId,
            @Param("productId") UUID productId,
            @Param("today") LocalDate today
    );
}
