package com.cognizant.insurance.claim_service.dto;

import jakarta.validation.constraints.NotBlank;

// Used by the admin to move a claim to APPROVED / REJECTED / IN_REVIEW.
public class ClaimStatusUpdateRequest {

    @NotBlank(message = "status is required (IN_REVIEW, APPROVED or REJECTED)")
    private String status;

    private String remarks;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
