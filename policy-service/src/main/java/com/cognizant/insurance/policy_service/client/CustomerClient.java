package com.cognizant.insurance.policy_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Declarative HTTP client. "customer-service" is resolved via Eureka, so we
// never hard-code its host/port. Feign builds the actual call at runtime.
@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);

    // Lets us turn the caller's auth user id (from the gateway) into the
    // customerId that properties and policies are keyed on.
    @GetMapping("/customers/user/{userId}")
    CustomerDto getByUserId(@PathVariable("userId") Long userId);
}
