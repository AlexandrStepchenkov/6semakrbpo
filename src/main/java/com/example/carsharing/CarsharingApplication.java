package com.example.carsharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.carsharing.model.AppUser;
import com.example.carsharing.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CarsharingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarsharingApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            AppUserRepository usersRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:admin}") String username,
            @Value("${app.admin.password:admin123}") String password,
            @Value("${app.admin.email:admin@carsharing.com}") String email
    ) {
        return args -> {
            if (usersRepository.findByUsername(username).isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRole("ROLE_ADMIN");
                admin.setEmail(email);

                usersRepository.save(admin);

                System.out.println(">>> Администратор создан из конфигурации. <<<");
            }
        };
    }
}