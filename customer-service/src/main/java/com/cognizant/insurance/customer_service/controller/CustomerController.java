package com.cognizant.insurance.customer_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.insurance.customer_service.dto.CustomerRequest;
import com.cognizant.insurance.customer_service.entity.Customer;
import com.cognizant.insurance.customer_service.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is running!");
    }

    // The gateway puts the caller's email in this header after it validates the token.
    // We use it as the "who created/updated this" audit value. It's optional so that
    // the service still works when hit directly during local testing.
    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody CustomerRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        Customer saved = customerService.createCustomer(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Customer> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(customerService.getByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id,
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request, userEmail));
    }
}
