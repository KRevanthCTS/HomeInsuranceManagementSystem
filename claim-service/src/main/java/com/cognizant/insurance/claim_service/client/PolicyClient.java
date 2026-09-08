package com.cognizant.insurance.claim_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "policy-service")
public interface PolicyClient {

    // Used to validate the policy number a customer types when filing a claim.
    @GetMapping("/policies/number/{policyNumber}")
    PolicyDto getByNumber(@PathVariable("policyNumber") String policyNumber);
}
