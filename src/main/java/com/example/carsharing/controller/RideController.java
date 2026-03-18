package com.example.carsharing.controller;

import com.example.carsharing.model.*;
import com.example.carsharing.service.DB;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {
    private final DB db;

    @PostMapping("/add")
    public Object add(@RequestParam int userId, @RequestParam int carId,
                      @RequestParam double dist, @RequestParam double hours) {
        Ride r = db.addRide(userId, carId, dist, hours);
        return r != null ? r : "car not available";
    }

    @PutMapping("/start")
    public Object start(@RequestParam Long rideId) {
        Ride r = db.startRide(rideId);
        return r != null ? r : "cannot start";
    }

    @PutMapping("/complete")
    public Object complete(@RequestParam Long rideId) {
        Payment p = db.completeRide(rideId);
        return p != null ? p : "cannot complete";
    }

    @GetMapping
    public List<Ride> all() { return db.rides(); }

    @GetMapping("/available-cars")
    public List<Car> availableCars() { return db.availableCars(); }

    @GetMapping("/my-rides")
    public List<Ride> myRides(Authentication auth) {
        String username = auth.getName();
        int userId = "admin".equals(username) ? 1 : 2;
        return db.ridesByUser(userId);
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Ride> ridesByUser(@PathVariable int userId) {
        return db.ridesByUser(userId);
    }

    @GetMapping("/revenue")
    public double revenue() { return db.totalRevenue(); }

    @GetMapping("/active-count")
    public int activeCount() { return db.activeRidesCount(); }

    @GetMapping("/busy-cars")
    public List<Car> busyCars() { return db.busyCars(); }
}