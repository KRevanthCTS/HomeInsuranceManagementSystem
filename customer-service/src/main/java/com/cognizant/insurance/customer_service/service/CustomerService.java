package com.cognizant.insurance.customer_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.customer_service.dto.CustomerRequest;
import com.cognizant.insurance.customer_service.entity.Customer;
import com.cognizant.insurance.customer_service.entity.Customer.RiskFactor;
import com.cognizant.insurance.customer_service.exception.BadRequestException;
import com.cognizant.insurance.customer_service.exception.DuplicateResourceException;
import com.cognizant.insurance.customer_service.exception.ResourceNotFoundException;
import com.cognizant.insurance.customer_service.repository.CustomerRepository;
import com.cognizant.insurance.customer_service.security.Caller;
import com.cognizant.insurance.customer_service.security.CallerContext;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CallerContext callerContext;

    public CustomerService(CustomerRepository customerRepository, CallerContext callerContext) {
        this.customerRepository = customerRepository;
        this.callerContext = callerContext;
    }

    public Customer createCustomer(CustomerRequest request, String updatedBy) {
        Caller caller = callerContext.current();

        // The profile belongs to whoever is logged in. An ADMIN may create one on
        // someone else's behalf by passing userId; a customer may not, so we
        // ignore the body's userId for them and use their own identity.
        Long ownerUserId = caller.isAdmin() ? request.getUserId() : caller.userId();
        if (ownerUserId == null) {
            throw new BadRequestException("userId could not be determined for this request");
        }

        if (customerRepository.existsByUserId(ownerUserId)) {
            throw new DuplicateResourceException(
                    "A customer profile already exists for user id " + ownerUserId);
        }

        Customer customer = new Customer();
        customer.setUserId(ownerUserId);
        customer.setFullName(request.getFullName());
        customer.setAge(request.getAge());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setRiskFactor(parseRiskFactor(request.getRiskFactor(), RiskFactor.LOW));
        customer.setUpdatedBy(updatedBy);

        return customerRepository.save(customer);
    }

    public Customer getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No customer with id " + id));
        callerContext.current().requireOwner(customer.getUserId(), "view customer " + id);
        return customer;
    }

    public Customer getByUserId(Long userId) {
        callerContext.current().requireOwner(userId, "view the profile of user " + userId);
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No customer for user id " + userId));
    }

    // Listing every customer is an admin-only view.
    public List<Customer> getAll() {
        callerContext.current().requireAdmin("list all customers");
        return customerRepository.findAll();
    }

    public Customer updateCustomer(Long id, CustomerRequest request, String updatedBy) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No customer with id " + id));
        callerContext.current().requireOwner(customer.getUserId(), "modify customer " + id);

        customer.setFullName(request.getFullName());
        customer.setAge(request.getAge());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        if (request.getRiskFactor() != null && !request.getRiskFactor().isBlank()) {
            customer.setRiskFactor(parseRiskFactor(request.getRiskFactor(), customer.getRiskFactor()));
        }
        customer.setUpdatedBy(updatedBy);
        return customerRepository.save(customer);
    }

    // Reported as a 400 listing the allowed values, rather than an unhandled
    // IllegalArgumentException surfacing as a 500.
    private RiskFactor parseRiskFactor(String raw, RiskFactor fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return RiskFactor.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid riskFactor '" + raw + "'. Allowed: LOW, MEDIUM, HIGH");
        }
    }
}
