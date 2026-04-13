package com.example.rbpo2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.rbpo2.model.AppUser;
import com.example.rbpo2.config.AdminProperties;

@SpringBootApplication
public class Rbpo2Application {

    @PersistenceContext
    private EntityManager entityManager;

    public static void main(String[] args) {
        SpringApplication.run(Rbpo2Application.class, args);
    }

    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner commandLineRunner(
            PasswordEncoder passwordEncoder,
            AdminProperties adminProperties
    ) {
        return args -> {
            if (entityManager.createQuery("select count(u) from AppUser u where u.username = :username", Long.class)
                    .setParameter("username", adminProperties.getUsername())
                    .getSingleResult() == 0) {
                AppUser admin = new AppUser();
                admin.setUsername(adminProperties.getUsername());
                admin.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
                admin.setRole("ROLE_ADMIN");
                admin.setEmail(adminProperties.getEmail());

                entityManager.persist(admin);

                System.out.println(">>> Администратор создан из конфигурации. <<<");
            }
        };
    }
}