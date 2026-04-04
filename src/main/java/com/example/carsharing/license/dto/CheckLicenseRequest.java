package com.example.carsharing.license.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class CheckLicenseRequest {
    private String deviceMac;
    private UUID productId;
}
