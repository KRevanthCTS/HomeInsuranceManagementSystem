package com.cognizant.insurance.policy_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.insurance.policy_service.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByCustomerId(Long customerId);
}
