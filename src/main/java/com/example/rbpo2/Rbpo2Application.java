package com.example.rbpo2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.rbpo2.config.AdminProperties;
import com.example.rbpo2.model.AppUser;
import com.example.rbpo2.repository.AppUserRepository;

@SpringBootApplication
public class Rbpo2Application {

    public static void main(String[] args) {
        SpringApplication.run(Rbpo2Application.class, args);
    }

    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner commandLineRunner(
            PasswordEncoder passwordEncoder,
            AdminProperties adminProperties,
            AppUserRepository appUserRepository
    ) {
        return args -> {
            if (appUserRepository.findByUsername(adminProperties.getUsername()).isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername(adminProperties.getUsername());
                admin.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
                admin.setRole("ROLE_ADMIN");
                admin.setEmail(adminProperties.getEmail());

                appUserRepository.save(admin);

                System.out.println(">>> Администратор создан из конфигурации. <<<");
            }
        };
    }
}