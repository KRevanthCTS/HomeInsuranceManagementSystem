package com.cognizant.insurance.policy_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.policy_service.dto.PropertyRequest;
import com.cognizant.insurance.policy_service.entity.Property;
import com.cognizant.insurance.policy_service.entity.Property.PropertyType;
import com.cognizant.insurance.policy_service.exception.ResourceNotFoundException;
import com.cognizant.insurance.policy_service.repository.PropertyRepository;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public Property createProperty(PropertyRequest request, String updatedBy) {
        Property property = new Property();
        property.setCustomerId(request.getCustomerId());
        property.setPropertyType(PropertyType.valueOf(request.getPropertyType().toUpperCase()));
        property.setBuiltUpArea(request.getBuiltUpArea());
        property.setConstructionYear(request.getConstructionYear());
        property.setPropertyValue(request.getPropertyValue());
        property.setHighRiskArea(request.isHighRiskArea());
        property.setBuildingNo(request.getBuildingNo());
        property.setStreet(request.getStreet());
        property.setCity(request.getCity());
        property.setState(request.getState());
        property.setZipCode(request.getZipCode());
        property.setUpdatedBy(updatedBy);
        return propertyRepository.save(property);
    }

    public Property getById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No property with id " + id));
    }

    public List<Property> getAll() {
        return propertyRepository.findAll();
    }

    public List<Property> getByCustomer(Long customerId) {
        return propertyRepository.findByCustomerId(customerId);
    }
}
