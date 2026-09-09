package com.cognizant.insurance.claim_service.client;

import java.math.BigDecimal;
import java.time.LocalDate;

// The parts of the policy JSON we need from policy-service: enough to confirm the
// policy exists, that it was in force when the incident happened, and that the
// claimed loss is within cover.
public class PolicyDto {

    private Long policyId;
    private String policyNumber;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal coverageAmount;

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }
}
