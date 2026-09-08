package com.cognizant.insurance.customer_service.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    // Points back to the user row in auth-service. It is just an id here -
    // there is no cross-database foreign key because each service owns its own DB.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Denormalised copy of the user's name so we don't have to call auth-service
    // every time we need to show/print the customer (e.g. on the policy PDF).
    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "phone_number", length = 25, nullable = false)
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    public enum RiskFactor {
        LOW, MEDIUM, HIGH
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_factor")
    private RiskFactor riskFactor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public Customer() {}

    public Long getCustomerId() {
        return customerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public RiskFactor getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(RiskFactor riskFactor) {
        this.riskFactor = riskFactor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
