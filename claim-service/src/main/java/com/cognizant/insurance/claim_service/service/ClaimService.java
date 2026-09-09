package com.cognizant.insurance.claim_service.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import com.cognizant.insurance.claim_service.exception.ConflictException;
import com.cognizant.insurance.claim_service.exception.ForbiddenException;
import com.cognizant.insurance.claim_service.exception.ResourceNotFoundException;
import com.cognizant.insurance.claim_service.repository.ClaimRepository;
import com.cognizant.insurance.claim_service.security.Caller;
import com.cognizant.insurance.claim_service.security.CallerContext;

import feign.FeignException;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);

    // The policy document promises claims are reported within 30 days of the
    // incident, so the service should hold callers to that.
    private static final int REPORTING_WINDOW_DAYS = 30;
    private static final int MAX_NUMBER_ATTEMPTS = 10;

    private final ClaimRepository claimRepository;
    private final PolicyClient policyClient;
    private final NotificationClient notificationClient;
    private final CallerContext callerContext;

    public ClaimService(ClaimRepository claimRepository,
            PolicyClient policyClient,
            NotificationClient notificationClient,
            CallerContext callerContext) {
        this.claimRepository = claimRepository;
        this.policyClient = policyClient;
        this.notificationClient = notificationClient;
        this.callerContext = callerContext;
    }

    public Claim submitClaim(ClaimRequest request, String customerEmail) {
        // 1. Confirm the policy exists and belongs to the caller. Our Feign call
        //    carries the caller's identity, so policy-service applies its own
        //    ownership rules - a customer cannot claim on someone else's policy.
        PolicyDto policy = lookUpPolicy(request.getPolicyNumber());

        // 2. Check the claim against the cover that policy actually provides.
        validateAgainstPolicy(request, policy);

        // 3. Reject a repeat of a claim already on file for the same incident.
        if (claimRepository.existsByPolicyIdAndIncidentTypeAndIncidentDate(
                policy.getPolicyId(), request.getIncidentType(), request.getIncidentDate())) {
            throw new ConflictException("A " + request.getIncidentType()
                    + " claim for policy " + policy.getPolicyNumber()
                    + " on " + request.getIncidentDate() + " has already been filed");
        }

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

        notify(saved.getCustomerEmail(),
                "Claim " + saved.getClaimNumber() + " received",
                "We have received your claim " + saved.getClaimNumber()
                        + " for policy " + saved.getPolicyNumber() + ". It is now being processed.");

        return saved;
    }

    // Approving or rejecting a claim is an assessor's decision, so this is
    // restricted to ADMIN callers - a customer must not be able to sign off
    // their own claim.
    public Claim updateStatus(Long claimId, ClaimStatusUpdateRequest request, String adminEmail) {
        callerContext.current().requireAdmin("change the status of a claim");

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("No claim with id " + claimId));

        ClaimStatus newStatus = parseStatus(request.getStatus());
        if (isSettled(claim.getClaimStatus())) {
            throw new ConflictException("Claim " + claim.getClaimNumber()
                    + " is already " + claim.getClaimStatus() + " and cannot be changed");
        }

        claim.setClaimStatus(newStatus);
        claim.setAdminRemarks(request.getRemarks());
        claim.setUpdatedBy(adminEmail);

        Claim saved = claimRepository.save(claim);

        notify(saved.getCustomerEmail(),
                "Claim " + saved.getClaimNumber() + " is now " + newStatus,
                "Your claim " + saved.getClaimNumber() + " has been marked "
                        + newStatus + "."
                        + (request.getRemarks() != null ? " Remarks: " + request.getRemarks() : ""));

        return saved;
    }

    public Claim getById(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No claim with id " + id));
        callerContext.current().requireOwnerEmail(claim.getCustomerEmail(), "view claim " + id);
        return claim;
    }

    // The full claim queue is the admin review list.
    public List<Claim> getAll() {
        callerContext.current().requireAdmin("list all claims");
        return claimRepository.findAll();
    }

    public List<Claim> getByStatus(String status) {
        callerContext.current().requireAdmin("list claims by status");
        return claimRepository.findByClaimStatus(parseStatus(status));
    }

    // A customer's own claims - the counterpart to the admin queue above.
    public List<Claim> getMyClaims() {
        Caller caller = callerContext.current();
        if (caller.email() == null || caller.email().isBlank()) {
            throw new ForbiddenException("Your token carries no email; please log in again");
        }
        return claimRepository.findByCustomerEmailOrderByCreatedAtDesc(caller.email());
    }

    // ---- helpers ----

    private void validateAgainstPolicy(ClaimRequest request, PolicyDto policy) {
        if (policy.getStatus() != null && !"ACTIVE".equalsIgnoreCase(policy.getStatus())) {
            throw new BadRequestException("Policy " + policy.getPolicyNumber()
                    + " is " + policy.getStatus() + "; only an ACTIVE policy can be claimed against");
        }

        LocalDate incident = request.getIncidentDate();
        LocalDate today = LocalDate.now();

        if (incident.isAfter(today)) {
            throw new BadRequestException("incidentDate " + incident + " is in the future");
        }
        if (policy.getStartDate() != null && incident.isBefore(policy.getStartDate())) {
            throw new BadRequestException("incidentDate " + incident
                    + " is before the policy start date of " + policy.getStartDate());
        }
        if (policy.getEndDate() != null && incident.isAfter(policy.getEndDate())) {
            throw new BadRequestException("incidentDate " + incident
                    + " is after the policy end date of " + policy.getEndDate());
        }
        if (ChronoUnit.DAYS.between(incident, today) > REPORTING_WINDOW_DAYS) {
            throw new BadRequestException("Claims must be reported within "
                    + REPORTING_WINDOW_DAYS + " days of the incident; " + incident + " is too long ago");
        }
        if (policy.getCoverageAmount() != null
                && request.getEstimatedLoss().compareTo(policy.getCoverageAmount()) > 0) {
            throw new BadRequestException("estimatedLoss " + request.getEstimatedLoss()
                    + " exceeds the policy coverage of " + policy.getCoverageAmount());
        }
    }

    private boolean isSettled(ClaimStatus status) {
        return status == ClaimStatus.APPROVED || status == ClaimStatus.REJECTED;
    }

    private PolicyDto lookUpPolicy(String policyNumber) {
        try {
            return policyClient.getByNumber(policyNumber);
        } catch (FeignException.NotFound e) {
            throw new BadRequestException("Unknown policy number: " + policyNumber);
        } catch (FeignException.Forbidden e) {
            // policy-service recognised the policy but it is not the caller's.
            throw new ForbiddenException("Policy " + policyNumber + " does not belong to you");
        }
    }

    private ClaimStatus parseStatus(String raw) {
        try {
            return ClaimStatus.valueOf(raw.trim().toUpperCase());
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

    // The column is unique, so on the rare collision we draw again rather than
    // surfacing a 500. The year keeps the space from filling up over time.
    private String generateClaimNumber() {
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            String candidate = String.format("CLM-%d-%06d",
                    LocalDate.now().getYear(),
                    ThreadLocalRandom.current().nextInt(0, 1_000_000));
            if (!claimRepository.existsByClaimNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Could not allocate a unique claim number after " + MAX_NUMBER_ATTEMPTS + " attempts");
    }
}
