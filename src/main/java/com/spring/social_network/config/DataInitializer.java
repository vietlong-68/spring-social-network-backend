package com.spring.social_network.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.spring.social_network.model.Role;
import com.spring.social_network.model.RoleType;
import com.spring.social_network.model.User;
import com.spring.social_network.model.Gender;
import com.spring.social_network.repository.RoleRepository;
import com.spring.social_network.repository.UserRepository;

import java.time.LocalDate;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType)) {
                Role role = Role.builder()
                        .name(roleType)
                        .build();

                roleRepository.save(role);
            }
        }
    }

    private void initializeAdminUser() {
        if (userRepository.existsByEmail("admin@example.com")) {
            return;
        }

        Set<Role> allRoles = roleRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toSet());

        if (allRoles.isEmpty()) {
            return;
        }

        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("12345678"))
                .firstName("Long")
                .lastName("Vt")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .phone("+84 123 456 789")
                .address("Hanoi, Vietnam")
                .roles(allRoles)
                .build();

        userRepository.save(adminUser);
    }
}