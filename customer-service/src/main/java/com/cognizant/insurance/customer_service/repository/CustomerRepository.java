package com.cognizant.insurance.customer_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.insurance.customer_service.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // A user has at most one customer profile (enforced by a unique constraint on
    // the column), so look-up by user id is handy.
    Optional<Customer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
