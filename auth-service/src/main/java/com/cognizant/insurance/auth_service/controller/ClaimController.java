package com.cognizant.insurance.auth_service.controller;

import com.cognizant.insurance.auth_service.dto.ClaimRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClaimController {

    @PostMapping("/claims")
    public String createClaim(@RequestBody ClaimRequest request) {
        return "Claim created successfully (secured endpoint)";
    }
}