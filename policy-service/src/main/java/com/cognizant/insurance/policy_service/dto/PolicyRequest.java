package com.cognizant.insurance.policy_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PolicyRequest {

    @NotNull(message = "propertyId is required")
    private Long propertyId;

    // e.g. FIRE, THEFT, NATURAL_DISASTER
    @NotBlank(message = "policyType (coverage type) is required")
    private String policyType;

    @NotNull(message = "coverageAmount is required")
    @Positive(message = "coverageAmount must be positive")
    private BigDecimal coverageAmount;

    // Optional - defaults to a 1 year term starting today if not supplied.
    private Integer durationYears;

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public Integer getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(Integer durationYears) {
        this.durationYears = durationYears;
    }
}
