// controller/TestController.java//
package com.cognizant.insurance.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String securedApi() {
        return "JWT is working!";
    }
}
