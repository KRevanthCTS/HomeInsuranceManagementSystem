package com.cognizant.insurance.claim_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.insurance.claim_service.dto.ClaimRequest;
import com.cognizant.insurance.claim_service.dto.ClaimStatusUpdateRequest;
import com.cognizant.insurance.claim_service.entity.Claim;
import com.cognizant.insurance.claim_service.entity.Claim.ClaimStatus;
import com.cognizant.insurance.claim_service.service.ClaimService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Claim Service is running!");
    }

    // Customer files a claim. The gateway forwards their email so we know who to notify.
    @PostMapping
    public ResponseEntity<Claim> submit(@Valid @RequestBody ClaimRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        Claim saved = claimService.submitClaim(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Admin view - all claims, or filter by status (e.g. ?status=SUBMITTED for the pending list).
    @GetMapping
    public ResponseEntity<List<Claim>> getAll(@RequestParam(required = false) ClaimStatus status) {
        if (status != null) {
            return ResponseEntity.ok(claimService.getByStatus(status));
        }
        return ResponseEntity.ok(claimService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getById(id));
    }

    // Admin approves / rejects / moves a claim to review.
    @PutMapping("/{id}/status")
    public ResponseEntity<Claim> updateStatus(@PathVariable Long id,
            @Valid @RequestBody ClaimStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String adminEmail) {
        return ResponseEntity.ok(claimService.updateStatus(id, request, adminEmail));
    }
}
