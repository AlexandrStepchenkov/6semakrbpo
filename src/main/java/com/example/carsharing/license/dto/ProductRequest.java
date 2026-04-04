package com.example.carsharing.license.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private Boolean isBlocked;
}
