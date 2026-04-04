package com.example.carsharing.license.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class CreateLicenseRequest {
    private UUID productId;
    private UUID typeId;
    private Long ownerId;
    private Integer deviceCount;
    private String description;
}
