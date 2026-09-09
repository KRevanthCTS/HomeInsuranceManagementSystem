package com.cognizant.insurance.policy_service.service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.policy_service.dto.PolicyRequest;
import com.cognizant.insurance.policy_service.entity.Policy;
import com.cognizant.insurance.policy_service.entity.Policy.Status;
import com.cognizant.insurance.policy_service.entity.Property;
import com.cognizant.insurance.policy_service.exception.ResourceNotFoundException;
import com.cognizant.insurance.policy_service.repository.PolicyRepository;

@Service
public class PolicyService {

    private static final int MAX_NUMBER_ATTEMPTS = 10;

    private final PolicyRepository policyRepository;
    private final PropertyService propertyService;
    private final PremiumCalculator premiumCalculator;

    public PolicyService(PolicyRepository policyRepository,
            PropertyService propertyService,
            PremiumCalculator premiumCalculator) {
        this.policyRepository = policyRepository;
        this.propertyService = propertyService;
        this.premiumCalculator = premiumCalculator;
    }

    public Policy createPolicy(PolicyRequest request, String updatedBy) {
        // The property must already exist - that's where all the premium inputs live.
        Property property = propertyService.getById(request.getPropertyId());

        Policy policy = new Policy();
        policy.setPolicyNumber(generatePolicyNumber());
        policy.setPropertyId(property.getPropertyId());
        policy.setPolicyType(request.getPolicyType());
        policy.setPremiumAmount(premiumCalculator.calculate(property));
        policy.setCoverageAmount(request.getCoverageAmount());

        int years = (request.getDurationYears() == null || request.getDurationYears() < 1)
                ? 1
                : request.getDurationYears();
        LocalDate today = LocalDate.now();
        policy.setStartDate(today);
        policy.setEndDate(today.plusYears(years));

        policy.setStatus(Status.ACTIVE);
        policy.setUpdatedBy(updatedBy);

        return policyRepository.save(policy);
    }

    public Policy getById(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No policy with id " + id));
        assertCallerOwns(policy);
        return policy;
    }

    public Policy getByNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No policy with number " + policyNumber));
        assertCallerOwns(policy);
        return policy;
    }

    // Listing every policy in the system is an admin-only view.
    public List<Policy> getAll() {
        propertyService.getAll();
        return policyRepository.findAll();
    }

    // A policy is owned by whoever owns its property. Reading the property runs
    // the ownership check for us and throws if the caller has no business here.
    private void assertCallerOwns(Policy policy) {
        propertyService.getById(policy.getPropertyId());
    }

    // Build a readable number e.g. HIP-2026-004213. The column is unique, so on
    // the rare collision we simply draw again rather than surfacing a 500.
    private String generatePolicyNumber() {
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            String candidate = String.format("HIP-%d-%06d",
                    Year.now().getValue(),
                    ThreadLocalRandom.current().nextInt(0, 1_000_000));
            if (!policyRepository.existsByPolicyNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Could not allocate a unique policy number after " + MAX_NUMBER_ATTEMPTS + " attempts");
    }
}
