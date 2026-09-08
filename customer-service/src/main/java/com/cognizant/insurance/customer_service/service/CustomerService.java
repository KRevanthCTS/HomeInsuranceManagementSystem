package com.cognizant.insurance.customer_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.customer_service.dto.CustomerRequest;
import com.cognizant.insurance.customer_service.entity.Customer;
import com.cognizant.insurance.customer_service.entity.Customer.RiskFactor;
import com.cognizant.insurance.customer_service.exception.ResourceNotFoundException;
import com.cognizant.insurance.customer_service.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(CustomerRequest request, String updatedBy) {
        Customer customer = new Customer();
        customer.setUserId(request.getUserId());
        customer.setFullName(request.getFullName());
        customer.setAge(request.getAge());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());

        // Default to LOW risk if the caller didn't say otherwise.
        if (request.getRiskFactor() != null && !request.getRiskFactor().isBlank()) {
            customer.setRiskFactor(RiskFactor.valueOf(request.getRiskFactor().toUpperCase()));
        } else {
            customer.setRiskFactor(RiskFactor.LOW);
        }
        customer.setUpdatedBy(updatedBy);

        return customerRepository.save(customer);
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No customer with id " + id));
    }

    public Customer getByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No customer for user id " + userId));
    }

    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    public Customer updateCustomer(Long id, CustomerRequest request, String updatedBy) {
        Customer customer = getById(id);
        customer.setFullName(request.getFullName());
        customer.setAge(request.getAge());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        if (request.getRiskFactor() != null && !request.getRiskFactor().isBlank()) {
            customer.setRiskFactor(RiskFactor.valueOf(request.getRiskFactor().toUpperCase()));
        }
        customer.setUpdatedBy(updatedBy);
        return customerRepository.save(customer);
    }
}
