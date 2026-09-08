package com.cognizant.insurance.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Single front door for the whole system. All external traffic comes in here,
// gets its JWT checked once (see JwtAuthenticationFilter) and is then routed
// to the right microservice using the names registered in Eureka.
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
