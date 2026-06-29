package com.studentmanagement.config;

import com.studentmanagement.model.AppUser;
import com.studentmanagement.model.enums.Role;
import com.studentmanagement.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin@studentmanagement.com", "admin123", Role.ADMIN);
        seedUser("faculty", "faculty@studentmanagement.com", "faculty123", Role.FACULTY);
        seedUser("student", "student@studentmanagement.com", "student123", Role.STUDENT);
    }

    private void seedUser(String username, String email, String rawPassword, Role role) {
        if (!appUserRepository.existsByUsername(username)) {
            appUserRepository.save(AppUser.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .enabled(true)
                    .build());
            System.out.println("Seeded user: " + username + " (" + role + ")");
        }
    }
}
