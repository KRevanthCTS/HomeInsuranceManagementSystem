package com.cognizant.insurance.claim_service.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cognizant.insurance.claim_service.client.NotificationClient;
import com.cognizant.insurance.claim_service.client.NotificationRequest;
import com.cognizant.insurance.claim_service.client.PolicyClient;
import com.cognizant.insurance.claim_service.client.PolicyDto;
import com.cognizant.insurance.claim_service.dto.ClaimRequest;
import com.cognizant.insurance.claim_service.dto.ClaimStatusUpdateRequest;
import com.cognizant.insurance.claim_service.entity.Claim;
import com.cognizant.insurance.claim_service.entity.Claim.ClaimStatus;
import com.cognizant.insurance.claim_service.exception.BadRequestException;
import com.cognizant.insurance.claim_service.exception.ResourceNotFoundException;
import com.cognizant.insurance.claim_service.repository.ClaimRepository;

import feign.FeignException;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claimRepository;
    private final PolicyClient policyClient;
    private final NotificationClient notificationClient;

    public ClaimService(ClaimRepository claimRepository,
            PolicyClient policyClient,
            NotificationClient notificationClient) {
        this.claimRepository = claimRepository;
        this.policyClient = policyClient;
        this.notificationClient = notificationClient;
    }

    public Claim submitClaim(ClaimRequest request, String customerEmail) {
        // 1. Confirm the policy actually exists by asking policy-service.
        PolicyDto policy = lookUpPolicy(request.getPolicyNumber());

        // 2. Save the claim in SUBMITTED state.
        Claim claim = new Claim();
        claim.setClaimNumber(generateClaimNumber());
        claim.setPolicyId(policy.getPolicyId());
        claim.setPolicyNumber(policy.getPolicyNumber());
        claim.setIncidentType(request.getIncidentType());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setDescription(request.getDescription());
        claim.setEstimatedLoss(request.getEstimatedLoss());
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        claim.setCustomerEmail(customerEmail);
        claim.setUpdatedBy(customerEmail);

        Claim saved = claimRepository.save(claim);

        // 3. Let the customer know we received it.
        notify(saved.getCustomerEmail(),
                "Claim " + saved.getClaimNumber() + " received",
                "We have received your claim " + saved.getClaimNumber()
                        + " for policy " + saved.getPolicyNumber() + ". It is now being processed.");

        return saved;
    }

    public Claim updateStatus(Long claimId, ClaimStatusUpdateRequest request, String adminEmail) {
        Claim claim = getById(claimId);

        ClaimStatus newStatus = parseStatus(request.getStatus());
        claim.setClaimStatus(newStatus);
        claim.setAdminRemarks(request.getRemarks());
        claim.setUpdatedBy(adminEmail);

        Claim saved = claimRepository.save(claim);

        // Tell the customer about the decision.
        notify(saved.getCustomerEmail(),
                "Claim " + saved.getClaimNumber() + " is now " + newStatus,
                "Your claim " + saved.getClaimNumber() + " has been marked "
                        + newStatus + "."
                        + (request.getRemarks() != null ? " Remarks: " + request.getRemarks() : ""));

        return saved;
    }

    public Claim getById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No claim with id " + id));
    }

    public List<Claim> getAll() {
        return claimRepository.findAll();
    }

    public List<Claim> getByStatus(ClaimStatus status) {
        return claimRepository.findByClaimStatus(status);
    }

    // ---- helpers ----

    private PolicyDto lookUpPolicy(String policyNumber) {
        try {
            return policyClient.getByNumber(policyNumber);
        } catch (FeignException.NotFound e) {
            throw new BadRequestException("Unknown policy number: " + policyNumber);
        }
    }

    private ClaimStatus parseStatus(String raw) {
        try {
            return ClaimStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status '" + raw
                    + "'. Allowed: SUBMITTED, IN_REVIEW, APPROVED, REJECTED");
        }
    }

    // Notifications are "best effort" - if notification-service is down we still
    // want the claim itself to succeed, so we swallow (but log) any failure.
    private void notify(String recipient, String subject, String message) {
        if (recipient == null || recipient.isBlank()) {
            return;
        }
        try {
            notificationClient.send(new NotificationRequest(recipient, subject, message));
        } catch (Exception e) {
            log.warn("Could not send notification to {}: {}", recipient, e.getMessage());
        }
    }

    private String generateClaimNumber() {
        int suffix = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("CLM-%06d", suffix);
    }
}
