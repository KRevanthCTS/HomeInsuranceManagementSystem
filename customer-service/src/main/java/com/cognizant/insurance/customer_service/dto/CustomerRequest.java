package com.cognizant.insurance.customer_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CustomerRequest {

    // Optional: for a normal customer this is taken from their token, so anything
    // sent here is ignored. Only an ADMIN can use it to create a profile on
    // someone else's behalf.
    private Long userId;

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotNull(message = "age is required")
    @Min(value = 18, message = "customer must be at least 18")
    private Integer age;

    @NotBlank(message = "phoneNumber is required")
    private String phoneNumber;

    @NotBlank(message = "address is required")
    private String address;

    // Optional - LOW / MEDIUM / HIGH. Used later when reviewing claims.
    private String riskFactor;

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

    public String getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(String riskFactor) {
        this.riskFactor = riskFactor;
    }
}
