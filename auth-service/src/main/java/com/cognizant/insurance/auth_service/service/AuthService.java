package com.cognizant.insurance.auth_service.service;

import com.cognizant.insurance.auth_service.dto.RegisterRequest;
import com.cognizant.insurance.auth_service.entity.User;
import com.cognizant.insurance.auth_service.entity.User.Role;
import com.cognizant.insurance.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User registerUser(RegisterRequest request, PasswordEncoder encoder) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(encoder.encode(request.getPassword()));
    // Convert the incoming role text to the enum value.
    // Note: this will throw IllegalArgumentException if the text is not a valid role
    // (expected values: "CUSTOMER" or "ADMIN")
    user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        return userRepository.save(user);
    }

    public User validateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }
}