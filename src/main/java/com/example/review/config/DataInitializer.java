package com.example.review.config;

import com.example.review.entity.Role;
import com.example.review.entity.User;
import com.example.review.entity.UserStatus;
import com.example.review.repository.RoleRepository;
import com.example.review.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createManagerAccount(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            Role managerRole = roleRepository
                    .findByName("MANAGER")
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Không tìm thấy role MANAGER"
                            )
                    );

            if (!userRepository.existsByUsernameIgnoreCase("admin")) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setFullName("Quản lý hệ thống");
                admin.setStatus(UserStatus.ACTIVE);
                admin.setRole(managerRole);
                admin.setBranch(null);

                userRepository.save(admin);

                System.out.println("Đã tạo tài khoản admin");
                }
         
        };
    }
}
