package com.example.carsharing.license.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    @JsonProperty("ticket")
    private Ticket ticket;

    @JsonProperty("signature")
    private String signature;
}
