package com.example.rbpo2.controller;

import com.example.rbpo2.model.User;
import com.example.rbpo2.service.DB;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final DB db;

    @PostMapping("/add")
    public User add(@RequestParam int userId,
                    @RequestParam String name,
                    @RequestParam String email,
                    @RequestParam String phone) {
        return db.saveUser(userId, name, email, phone);
    }

    @GetMapping
    public List<User> all() {
        return db.users();
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam int userId) {
        db.deleteUser(userId);
        return "deleted";
    }
}