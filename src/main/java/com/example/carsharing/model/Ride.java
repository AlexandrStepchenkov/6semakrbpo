package com.example.carsharing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rideId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "car_id", nullable = false)
    private int carId;

    @Column(name = "distance_km", nullable = false)
    private double distanceKm;

    @Column(name = "duration_hours", nullable = false)
    private double durationHours;

    @Column(nullable = false)
    private String status; // CREATED, IN_PROGRESS, COMPLETED
}