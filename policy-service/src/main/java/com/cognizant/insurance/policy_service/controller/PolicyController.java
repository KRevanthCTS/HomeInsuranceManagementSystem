package com.cognizant.insurance.policy_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.insurance.policy_service.dto.PolicyRequest;
import com.cognizant.insurance.policy_service.entity.Policy;
import com.cognizant.insurance.policy_service.entity.Property;
import com.cognizant.insurance.policy_service.service.PolicyPdfService;
import com.cognizant.insurance.policy_service.service.PolicyService;
import com.cognizant.insurance.policy_service.service.PremiumCalculator;
import com.cognizant.insurance.policy_service.service.PropertyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService policyService;
    private final PropertyService propertyService;
    private final PremiumCalculator premiumCalculator;
    private final PolicyPdfService policyPdfService;

    public PolicyController(PolicyService policyService,
            PropertyService propertyService,
            PremiumCalculator premiumCalculator,
            PolicyPdfService policyPdfService) {
        this.policyService = policyService;
        this.propertyService = propertyService;
        this.premiumCalculator = premiumCalculator;
        this.policyPdfService = policyPdfService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Policy Service is running!");
    }

    @PostMapping
    public ResponseEntity<Policy> create(@Valid @RequestBody PolicyRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        Policy saved = policyService.createPolicy(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Policy>> getAll() {
        return ResponseEntity.ok(policyService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Policy> getById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getById(id));
    }

    @GetMapping("/number/{policyNumber}")
    public ResponseEntity<Policy> getByNumber(@PathVariable String policyNumber) {
        return ResponseEntity.ok(policyService.getByNumber(policyNumber));
    }

    // Premium preview - lets the frontend show the price before the policy is actually bought.
    @GetMapping("/quote")
    public ResponseEntity<Map<String, Object>> quote(@RequestParam Long propertyId) {
        Property property = propertyService.getById(propertyId);
        return ResponseEntity.ok(Map.of(
                "propertyId", propertyId,
                "riskFactor", premiumCalculator.riskFactorFor(property),
                "annualPremium", premiumCalculator.calculate(property)
        ));
    }

    // Downloadable mock policy PDF (requirement 5 in the brief).
    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        byte[] pdf = policyPdfService.generate(id);
        Policy policy = policyService.getById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", policy.getPolicyNumber() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
