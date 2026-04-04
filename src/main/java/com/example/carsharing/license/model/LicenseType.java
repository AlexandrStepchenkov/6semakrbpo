package com.example.carsharing.license.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "license_type")
public class LicenseType {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "default_duration_in_days", nullable = false)
    private int defaultDurationInDays;

    @Column(name = "description")
    private String description;

    @PrePersist
    @SuppressWarnings("unused")
    private void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
