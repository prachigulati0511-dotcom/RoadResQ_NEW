package com.roadresq.service;

import com.roadresq.dto.CostEstimateRequestDto;
import com.roadresq.dto.CostEstimateResponseDto;
import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CostEstimatorTest {

    private final CostEstimatorService costEstimatorService = new CostEstimatorService();

    @Test
    @DisplayName("Verify transparent itemized pricing calculation")
    void testTransparentPricing() {
        CostEstimateRequestDto req = new CostEstimateRequestDto();
        req.setServiceType(ServiceType.TOWING);
        req.setSeverity(Severity.CRITICAL_HAZARD);
        // Distance ~ 10 km
        req.setCustomerLat(37.7749);
        req.setCustomerLon(-122.4194);
        req.setProviderLat(37.8500);
        req.setProviderLon(-122.4194);

        CostEstimateResponseDto estimate = costEstimatorService.calculateEstimate(req);

        assertEquals(new BigDecimal("1499.00"), estimate.getBaseServiceFee());
        assertEquals(new BigDecimal("350.00"), estimate.getHazardSurcharge());
        assertTrue(estimate.getTravelDistanceKm() > 0);
        assertTrue(estimate.getTravelFee().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(estimate.getTotalEstimatedCost().compareTo(estimate.getSubtotal()) > 0);
        assertNotNull(estimate.getPricingNotice());
    }
}
