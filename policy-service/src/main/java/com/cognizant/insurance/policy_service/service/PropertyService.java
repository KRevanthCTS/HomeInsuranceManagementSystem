package com.cognizant.insurance.policy_service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cognizant.insurance.policy_service.dto.PropertyRequest;
import com.cognizant.insurance.policy_service.entity.Property;
import com.cognizant.insurance.policy_service.entity.Property.PropertyType;
import com.cognizant.insurance.policy_service.exception.BadRequestException;
import com.cognizant.insurance.policy_service.exception.ResourceNotFoundException;
import com.cognizant.insurance.policy_service.repository.PropertyRepository;

@Service
public class PropertyService {

    // A property older than this is almost certainly a typo rather than a manor
    // house, and a construction year in the future is never right.
    private static final int OLDEST_PLAUSIBLE_YEAR = 1800;

    private final PropertyRepository propertyRepository;
    private final OwnershipService ownership;

    public PropertyService(PropertyRepository propertyRepository, OwnershipService ownership) {
        this.propertyRepository = propertyRepository;
        this.ownership = ownership;
    }

    public Property createProperty(PropertyRequest request, String updatedBy) {
        // The property belongs to the caller. An ADMIN may register one on
        // someone else's behalf by passing customerId; a customer may not, so
        // their own profile is used regardless of what the body said.
        Long callerCustomerId = ownership.callerCustomerId();
        Long ownerCustomerId = (callerCustomerId == null) ? request.getCustomerId() : callerCustomerId;
        if (ownerCustomerId == null) {
            throw new BadRequestException("customerId is required when registering on behalf of a customer");
        }

        Property property = new Property();
        property.setCustomerId(ownerCustomerId);
        property.setPropertyType(parsePropertyType(request.getPropertyType()));
        property.setBuiltUpArea(request.getBuiltUpArea());
        property.setConstructionYear(validConstructionYear(request.getConstructionYear()));
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
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No property with id " + id));
        ownership.requireOwns(property.getCustomerId(), "view property " + id);
        return property;
    }

    // Listing every property in the system is an admin-only view.
    public List<Property> getAll() {
        ownership.requireAdmin("list all properties");
        return propertyRepository.findAll();
    }

    public List<Property> getByCustomer(Long customerId) {
        ownership.requireOwns(customerId, "list the properties of customer " + customerId);
        return propertyRepository.findByCustomerId(customerId);
    }

    // Reported as a 400 listing the allowed values, rather than an unhandled
    // IllegalArgumentException surfacing as a 500.
    private PropertyType parsePropertyType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("propertyType is required (APARTMENT or HOUSE)");
        }
        try {
            return PropertyType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid propertyType '" + raw + "'. Allowed: APARTMENT, HOUSE");
        }
    }

    private Integer validConstructionYear(Integer year) {
        int thisYear = LocalDate.now().getYear();
        if (year == null || year < OLDEST_PLAUSIBLE_YEAR || year > thisYear) {
            throw new BadRequestException("constructionYear must be between "
                    + OLDEST_PLAUSIBLE_YEAR + " and " + thisYear);
        }
        return year;
    }
}
