package com.cognizant.insurance.auth_service.controller;

import com.cognizant.insurance.auth_service.dto.ClaimRequest;
import com.cognizant.insurance.auth_service.entity.Claim;
import com.cognizant.insurance.auth_service.repository.ClaimRepository;
import com.cognizant.insurance.auth_service.service.ClaimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClaimController {

    private final ClaimService claimService;
    private final ClaimRepository claimRepository;

    public ClaimController(ClaimService claimService, ClaimRepository claimRepository) {
        this.claimService = claimService;
        this.claimRepository = claimRepository;
    }

    //Written for testing purpose, will be removed later
    // @PostMapping("/claims")
    // public String createClaim(@RequestBody ClaimRequest request) {
    //     return "Claim created successfully (secured endpoint)";
    // }

    @PostMapping("/claims")
    public ResponseEntity<?> createClaim(@RequestBody ClaimRequest request) {
        return ResponseEntity.ok(claimService.createClaim(request));
    }

    @GetMapping("/claims")
    public ResponseEntity<?> getAllClaims() {
        return ResponseEntity.ok(claimRepository.findAll());
    }
}