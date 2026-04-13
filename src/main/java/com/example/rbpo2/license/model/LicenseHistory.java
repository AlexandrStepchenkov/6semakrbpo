package com.example.rbpo2.license.model;

import java.time.LocalDate;
import java.util.UUID;

import com.example.rbpo2.model.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "license_history")
public class LicenseHistory {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;

    @Column(name = "description")
    private String description;

    @PrePersist
    @SuppressWarnings("unused")
    private void ensureIdAndDate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (changeDate == null) {
            changeDate = LocalDate.now();
        }
    }
}
