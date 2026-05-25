package com.cognizant.insurance.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognizant.insurance.auth_service.entity.Claim;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
}
