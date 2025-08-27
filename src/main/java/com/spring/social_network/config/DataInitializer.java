package com.spring.social_network.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.spring.social_network.model.Role;
import com.spring.social_network.model.RoleType;
import com.spring.social_network.model.User;
import com.spring.social_network.model.Gender;
import com.spring.social_network.repository.RoleRepository;
import com.spring.social_network.repository.UserRepository;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

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
    @Transactional
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    @Transactional
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

    @Transactional
    private void initializeAdminUser() {
        if (userRepository.existsByEmail("admin@example.com")) {
            return;
        }

        Set<Role> allRoles = new HashSet<>(roleRepository.findAll());

        if (allRoles.isEmpty()) {
            return;
        }

        User adminUser = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("12345678"))
                .firstName("Admin")
                .lastName("Dev")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .phone("+84 123 456 789")
                .address("Hanoi, Vietnam")
                .roles(allRoles)
                .build();

        userRepository.save(adminUser);
    }
}