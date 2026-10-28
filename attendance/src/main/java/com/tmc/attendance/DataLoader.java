// https://www.geeksforgeeks.org/advance-java/spring-boot-load-initial-data/
package com.tmc.attendance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tmc.attendance.user.AppUser;
import com.tmc.attendance.user.UserRepository;
import com.tmc.attendance.user.UserRole;

@Component
public class DataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private String adminEmail = "admin@tmc.com";

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            AppUser admin = new AppUser();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("password"));
            admin.setRole(UserRole.ADMIN);
            admin.setName("Admin");
            admin.setActive(true);
            userRepository.save(admin);
        }
    }

}
