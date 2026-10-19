package com.roadresq.service;

import com.roadresq.dto.CostEstimateRequestDto;
import com.roadresq.dto.CostEstimateResponseDto;
import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import com.roadresq.util.GeoUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostEstimatorService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18"); // 18% Indian GST
    private static final double FREE_TRAVEL_KM = 5.0;
    private static final BigDecimal PER_KM_RATE = new BigDecimal("25.00"); // ₹25 per billable km

    public BigDecimal getBaseFee(ServiceType serviceType) {
        if (serviceType == null) {
            return new BigDecimal("499.00");
        }
        return switch (serviceType) {
            case FLAT_TYRE -> new BigDecimal("399.00");
            case BATTERY_JUMP -> new BigDecimal("499.00");
            case FUEL_DELIVERY -> new BigDecimal("399.00");
            case TOWING -> new BigDecimal("1499.00");
            case ENGINE_BREAKDOWN -> new BigDecimal("799.00");
            case OVERHEATING -> new BigDecimal("699.00");
            case LOCKOUT -> new BigDecimal("499.00");
            case EV_CHARGE -> new BigDecimal("799.00");
            case ACCIDENT_ASSISTANCE -> new BigDecimal("2499.00");
            case OTHER -> new BigDecimal("499.00");
        };
    }

    public BigDecimal getHazardFee(Severity severity) {
        if (severity == null) {
            return BigDecimal.ZERO;
        }
        return switch (severity) {
            case NORMAL -> BigDecimal.ZERO;
            case URGENT -> new BigDecimal("150.00");
            case CRITICAL_HAZARD -> new BigDecimal("350.00");
        };
    }

    public BigDecimal calculateTravelFee(double distanceKm) {
        if (distanceKm <= FREE_TRAVEL_KM) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        double billableKm = distanceKm - FREE_TRAVEL_KM;
        return PER_KM_RATE.multiply(BigDecimal.valueOf(billableKm)).setScale(2, RoundingMode.HALF_UP);
    }

    public CostEstimateResponseDto calculateEstimate(CostEstimateRequestDto request) {
        BigDecimal baseFee = getBaseFee(request.getServiceType());
        BigDecimal hazardFee = getHazardFee(request.getSeverity());

        double distanceKm = 0.0;
        if (request.getCustomerLat() != null && request.getCustomerLon() != null &&
            request.getProviderLat() != null && request.getProviderLon() != null) {
            distanceKm = GeoUtils.calculateDistanceKm(
                    request.getCustomerLat(), request.getCustomerLon(),
                    request.getProviderLat(), request.getProviderLon()
            );
        } else {
            // Default estimated urban radius if provider not yet selected
            distanceKm = 6.4;
        }

        BigDecimal travelFee = calculateTravelFee(distanceKm);
        BigDecimal subtotal = baseFee.add(travelFee).add(hazardFee);
        BigDecimal estimatedTax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(estimatedTax).setScale(2, RoundingMode.HALF_UP);

        return new CostEstimateResponseDto(baseFee, distanceKm, travelFee, hazardFee, subtotal, estimatedTax, total);
    }
}
