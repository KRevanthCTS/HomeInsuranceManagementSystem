package com.cognizant.insurance.policy_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.cognizant.insurance.policy_service.entity.Property;
import com.cognizant.insurance.policy_service.entity.Property.PropertyType;

// Home insurance premium rule (from the project brief):
//
//   Premium = Base Rate * Property Value * Risk Factor
//
//   Base Rate    = 0.5% of property value
//   Risk Factor  = 1.0 for an apartment, 1.2 for an independent house
//                  + 0.2 if the house is old (> 20 years)
//                  + 0.3 if it is in a high-risk area
//
// Worked example from the brief: value 50,00,000, house(1.2) + old(0.2) = 1.4
//   => 0.5% * 50,00,000 * 1.4 = 35,000
@Component
public class PremiumCalculator {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.005"); // 0.5%
    private static final int OLD_PROPERTY_YEARS = 20;

    public BigDecimal calculate(Property property) {
        BigDecimal riskFactor = riskFactorFor(property);

        BigDecimal premium = property.getPropertyValue()
                .multiply(BASE_RATE)
                .multiply(riskFactor);

        // Money - keep it to 2 decimal places.
        return premium.setScale(2, RoundingMode.HALF_UP);
    }

    // Exposed separately so the /policies/quote endpoint can explain the number.
    public BigDecimal riskFactorFor(Property property) {
        BigDecimal factor = (property.getPropertyType() == PropertyType.HOUSE)
                ? new BigDecimal("1.2")
                : new BigDecimal("1.0");

        int age = LocalDate.now().getYear() - property.getConstructionYear();
        if (age > OLD_PROPERTY_YEARS) {
            factor = factor.add(new BigDecimal("0.2"));
        }

        if (property.isHighRiskArea()) {
            factor = factor.add(new BigDecimal("0.3"));
        }

        return factor;
    }
}
