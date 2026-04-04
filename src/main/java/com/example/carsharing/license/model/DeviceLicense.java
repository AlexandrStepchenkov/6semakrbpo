package com.example.carsharing.license.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(
    name = "device_license",
    uniqueConstraints = @UniqueConstraint(name = "uk_device_license_license_device", columnNames = {"license_id", "device_id"})
)
public class DeviceLicense {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private LicenseDevice device;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @PrePersist
    @SuppressWarnings("unused")
    private void ensureIdAndDate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (activationDate == null) {
            activationDate = LocalDate.now();
        }
    }
}
