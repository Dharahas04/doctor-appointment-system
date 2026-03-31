package com.app.doctorappointment.config;

import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.model.User;
import com.app.doctorappointment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdminConfig {

    private static final String LEGACY_BOOTSTRAP_ADMIN_EMAIL = "admin@pulsepoint.local";

    @Bean
    public CommandLineRunner bootstrapAdmin(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.email}") String adminEmail,
            @Value("${app.bootstrap.admin.password}") String adminPassword,
            @Value("${app.bootstrap.admin.full-name}") String adminFullName) {
        return args -> {
            String normalizedEmail = adminEmail.trim().toLowerCase();

            if (!LEGACY_BOOTSTRAP_ADMIN_EMAIL.equalsIgnoreCase(normalizedEmail)) {
                userRepository.findByEmailIgnoreCase(LEGACY_BOOTSTRAP_ADMIN_EMAIL).ifPresent(legacyAdmin -> {
                    if (legacyAdmin.getRole() == Role.Admin) {
                        legacyAdmin.setIsActive(false);
                        userRepository.save(legacyAdmin);
                    }
                });
            }

            User admin = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(User::new);
            admin.setFullName(adminFullName);
            admin.setEmail(normalizedEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.Admin);
            admin.setIsActive(true);
            userRepository.save(admin);
        };
    }
}
