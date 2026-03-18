package com.example.carsharing.controller;

import com.example.carsharing.model.Car;
import com.example.carsharing.service.DB;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {
    private final DB db;

    @PostMapping("/add")
    public Car add(@RequestParam int carId,
                   @RequestParam String model,
                   @RequestParam String licensePlate,
                   @RequestParam double pricePerKm) {
        return db.saveCar(carId, model, licensePlate, pricePerKm);
    }

    @GetMapping
    public List<Car> all() {
        return db.cars();
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam int carId) {
        db.deleteCar(carId);
        return "car disabled";
    }
}