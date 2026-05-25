package com.cognizant.insurance.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;

    @Column(name = "claim_number")
    private String claimNumber;

    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "incident_type")
    private String incidentType;

    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @Column(name = "estimated_loss")
    private Double estimatedLoss;

    @Column(name = "claim_status")
    private String claimStatus;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // getters & setters

    public Long getClaimId() {
        return claimId;
    }

    public String getClaimNumber() {
        return claimNumber;
    }
    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public Long getPolicyId() {
        return policyId;
    }
    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
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

    public Double getEstimatedLoss() {
        return estimatedLoss;
    }
    public void setEstimatedLoss(Double estimatedLoss) {
        this.estimatedLoss = estimatedLoss;
    }

    public String getClaimStatus() {
        return claimStatus;
    }
    public void setClaimStatus(String claimStatus) {
        this.claimStatus = claimStatus;
    }
}