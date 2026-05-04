package com.cognizant.insurance.auth_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/auth")
public class TestController {


    @PostMapping("/test")
    public String securedApi() {
        return "JWT is working!";
    }
}
