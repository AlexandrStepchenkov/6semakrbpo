package com.example.rbpo2.license.dto;

import lombok.Data;

@Data
public class LicenseTypeRequest {
    private String name;
    private Integer defaultDurationInDays;
    private String description;
}
