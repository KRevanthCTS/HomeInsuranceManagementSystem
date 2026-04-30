// service/AuthService.java
package com.cognizant.insurance.auth_service.service;

import com.cognizant.insurance.auth_service.entity.User;
import com.cognizant.insurance.auth_service.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User validateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }
}