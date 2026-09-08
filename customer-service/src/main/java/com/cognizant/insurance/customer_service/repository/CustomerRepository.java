package com.cognizant.insurance.customer_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.insurance.customer_service.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // A user should only have one customer profile, so look-up by user id is handy.
    Optional<Customer> findByUserId(Long userId);
}
