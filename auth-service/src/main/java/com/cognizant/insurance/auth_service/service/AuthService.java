package com.cognizant.insurance.auth_service.service;

import com.cognizant.insurance.auth_service.dto.RegisterRequest;
import com.cognizant.insurance.auth_service.entity.User;
import com.cognizant.insurance.auth_service.entity.User.Role;
import com.cognizant.insurance.auth_service.exception.BadRequestException;
import com.cognizant.insurance.auth_service.exception.DuplicateResourceException;
import com.cognizant.insurance.auth_service.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
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
            throw new DuplicateResourceException(
                    "An account with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(encoder.encode(request.getPassword()));
        user.setRole(parseRole(request.getRole()));

        return userRepository.save(user);
    }

    public User validateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    // Reported as a 400 with the allowed values rather than an unhandled
    // IllegalArgumentException.
    private Role parseRole(String raw) {
        try {
            return Role.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid role '" + raw + "'. Allowed: CUSTOMER, ADMIN");
        }
    }
}