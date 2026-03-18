package com.example.carsharing.controller;

import com.example.carsharing.model.Payment;
import com.example.carsharing.service.DB;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final DB db;

    @GetMapping
    public List<Payment> all() {
        return db.payments();
    }
}