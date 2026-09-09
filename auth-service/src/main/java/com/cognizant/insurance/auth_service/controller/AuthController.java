package com.cognizant.insurance.auth_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.insurance.auth_service.dto.LoginRequest;
import com.cognizant.insurance.auth_service.dto.LoginResponse;
import com.cognizant.insurance.auth_service.dto.RegisterRequest;
import com.cognizant.insurance.auth_service.entity.User;
import com.cognizant.insurance.auth_service.service.AuthService;
import com.cognizant.insurance.auth_service.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth Service is running!");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // AuthenticationManager delegates to CustomUserDetailsService and checks
            // the password against the stored BCrypt hash, so there is no need to
            // re-verify it here.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = authService.validateUser(request.getEmail());

            String token = jwtUtil.generateToken(
                    user.getEmail(), user.getRole().name(), user.getUserId());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request, passwordEncoder);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
