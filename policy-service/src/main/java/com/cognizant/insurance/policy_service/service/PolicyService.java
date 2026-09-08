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
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No policy with id " + id));
    }

    public Policy getByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No policy with number " + policyNumber));
    }

    public List<Policy> getAll() {
        return policyRepository.findAll();
    }

    // Build a readable, reasonably unique number e.g. HIP-2026-004213
    private String generatePolicyNumber() {
        int suffix = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("HIP-%d-%06d", Year.now().getValue(), suffix);
    }
}
