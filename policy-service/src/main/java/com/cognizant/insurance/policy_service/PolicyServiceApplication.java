package com.cognizant.insurance.policy_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// @EnableFeignClients switches on the declarative REST clients (see client package)
// so this service can call customer-service through Eureka.
@SpringBootApplication
@EnableFeignClients
public class PolicyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolicyServiceApplication.class, args);
	}

}
