package com.example.rbpo2.license.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class Ticket {
    private LocalDateTime serverTime;
    private long ticketLifetimeSeconds;
    private LocalDate firstActivationDate;
    private LocalDate expirationDate;
    private Long userId;
    private UUID deviceId;
    private boolean blocked;
}
