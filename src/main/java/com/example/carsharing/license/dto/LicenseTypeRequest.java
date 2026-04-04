package com.example.carsharing.license.dto;

import lombok.Data;

@Data
public class LicenseTypeRequest {
    private String name;
    private Integer defaultDurationInDays;
    private String description;
}
