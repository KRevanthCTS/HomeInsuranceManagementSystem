package com.cognizant.insurance.claim_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ClaimRequest {

    @NotBlank(message = "policyNumber is required")
    private String policyNumber;

    @NotBlank(message = "incidentType is required")
    private String incidentType;

    @NotNull(message = "incidentDate is required (yyyy-MM-dd)")
    private LocalDate incidentDate;

    private String description;

    @NotNull(message = "estimatedLoss is required")
    @Positive(message = "estimatedLoss must be positive")
    private BigDecimal estimatedLoss;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getEstimatedLoss() {
        return estimatedLoss;
    }

    public void setEstimatedLoss(BigDecimal estimatedLoss) {
        this.estimatedLoss = estimatedLoss;
    }
}
