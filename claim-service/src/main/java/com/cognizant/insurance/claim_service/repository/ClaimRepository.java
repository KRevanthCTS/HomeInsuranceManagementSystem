package com.cognizant.insurance.claim_service.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.insurance.claim_service.entity.Claim;
import com.cognizant.insurance.claim_service.entity.Claim.ClaimStatus;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    boolean existsByClaimNumber(String claimNumber);

    // Handy for the admin dashboard's "pending claims" view.
    List<Claim> findByClaimStatus(ClaimStatus claimStatus);

    // A customer's own claims.
    List<Claim> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    // Guards against the same incident being claimed twice.
    boolean existsByPolicyIdAndIncidentTypeAndIncidentDate(
            Long policyId, String incidentType, LocalDate incidentDate);
}
