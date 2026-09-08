package com.cognizant.insurance.claim_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.insurance.claim_service.entity.Claim;
import com.cognizant.insurance.claim_service.entity.Claim.ClaimStatus;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    // Handy for the admin dashboard's "pending claims" view.
    List<Claim> findByClaimStatus(ClaimStatus claimStatus);
}
