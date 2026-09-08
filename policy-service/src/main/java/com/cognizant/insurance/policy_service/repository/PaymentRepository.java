package com.cognizant.insurance.policy_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.insurance.policy_service.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPolicyId(Long policyId);
}
