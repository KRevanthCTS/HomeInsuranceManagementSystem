package com.cognizant.insurance.claim_service.client;

// Subset of the policy JSON returned by policy-service - just enough to confirm
// the policy exists and to grab its internal id.
public class PolicyDto {

    private Long policyId;
    private String policyNumber;
    private String status;

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
}
