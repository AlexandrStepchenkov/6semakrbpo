package com.example.rbpo2.license.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class CheckLicenseRequest {
    private String deviceMac;
    private UUID productId;
}
