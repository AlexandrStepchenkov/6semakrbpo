package com.example.rbpo2.license.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LicenseResponse {
    private UUID id;

    @JsonProperty("activationKey")
    private String code;

    private UUID productId;
    private UUID typeId;
    private Long ownerId;
    private Long userId;
    private LocalDate firstActivationDate;
    private LocalDate endingDate;
    private boolean blocked;
    private int deviceCount;
    private String description;
}
