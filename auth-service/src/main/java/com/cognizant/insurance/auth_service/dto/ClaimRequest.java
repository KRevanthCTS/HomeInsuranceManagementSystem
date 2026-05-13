package com.cognizant.insurance.auth_service.dto;

public class ClaimRequest {

    private String incidentType;
    private String incidentDate;
    private double estimatedLoss;

    public String getIncidentType() {
        return incidentType;
    }
    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }
    public String getIncidentDate() {
        return incidentDate;
    }
    public void setIncidentDate(String incidentDate) {
        this.incidentDate = incidentDate;
    }
    public double getEstimatedLoss() {
        return estimatedLoss;
    }
    public void setEstimatedLoss(double estimatedLoss) {
        this.estimatedLoss = estimatedLoss;
    }
}