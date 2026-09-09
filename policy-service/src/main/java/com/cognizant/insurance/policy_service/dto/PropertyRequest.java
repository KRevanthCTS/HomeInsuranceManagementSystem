package com.cognizant.insurance.policy_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PropertyRequest {

    // Optional: for a normal customer this is derived from their token, so
    // anything sent here is ignored. Only an ADMIN can use it to register a
    // property on someone else's behalf.
    private Long customerId;

    @NotBlank(message = "propertyType is required (APARTMENT or HOUSE)")
    private String propertyType;

    @NotNull(message = "builtUpArea is required")
    @Positive(message = "builtUpArea must be positive")
    private Integer builtUpArea;

    @NotNull(message = "constructionYear is required")
    private Integer constructionYear;

    @NotNull(message = "propertyValue is required")
    @Positive(message = "propertyValue must be positive")
    private BigDecimal propertyValue;

    // Optional flag that drives the "+0.3 high-risk area" premium loading.
    private boolean highRiskArea;

    private String buildingNo;
    private String street;
    private String city;
    private String state;
    private String zipCode;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getBuiltUpArea() {
        return builtUpArea;
    }

    public void setBuiltUpArea(Integer builtUpArea) {
        this.builtUpArea = builtUpArea;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public BigDecimal getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(BigDecimal propertyValue) {
        this.propertyValue = propertyValue;
    }

    public boolean isHighRiskArea() {
        return highRiskArea;
    }

    public void setHighRiskArea(boolean highRiskArea) {
        this.highRiskArea = highRiskArea;
    }

    public String getBuildingNo() {
        return buildingNo;
    }

    public void setBuildingNo(String buildingNo) {
        this.buildingNo = buildingNo;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
