package com.cognizant.insurance.policy_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.insurance.policy_service.dto.PropertyRequest;
import com.cognizant.insurance.policy_service.entity.Property;
import com.cognizant.insurance.policy_service.service.PropertyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<Property> create(@Valid @RequestBody PropertyRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        Property saved = propertyService.createProperty(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Property>> getAll(@RequestParam(required = false) Long customerId) {
        if (customerId != null) {
            return ResponseEntity.ok(propertyService.getByCustomer(customerId));
        }
        return ResponseEntity.ok(propertyService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }
}
