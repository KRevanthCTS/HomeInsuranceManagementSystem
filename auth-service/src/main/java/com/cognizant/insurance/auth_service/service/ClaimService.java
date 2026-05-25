package com.cognizant.insurance.auth_service.service;

import com.cognizant.insurance.auth_service.dto.ClaimRequest;
import com.cognizant.insurance.auth_service.entity.Claim;
import com.cognizant.insurance.auth_service.repository.ClaimRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;

    public ClaimService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    public Claim createClaim(ClaimRequest request) {

        Claim claim = new Claim();
        claim.setClaimNumber("CLM-" + System.currentTimeMillis());
        claim.setPolicyId(1L); // TEMP
        claim.setIncidentType(request.getIncidentType());
        claim.setIncidentDate(LocalDate.parse(request.getIncidentDate()));
        claim.setEstimatedLoss(request.getEstimatedLoss());
        claim.setClaimStatus("SUBMITTED");

        return claimRepository.save(claim);
    }
}