package com.cognizant.insurance.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PolicyController {

    @GetMapping("/policies")
    public String getPolicies() {
        return "Policies fetched successfully (secured endpoint)";
    }
}