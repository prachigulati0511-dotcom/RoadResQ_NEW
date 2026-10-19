package com.roadresq.dto;

import java.math.BigDecimal;

public class CostEstimateResponseDto {
    private BigDecimal baseServiceFee;
    private Double travelDistanceKm;
    private BigDecimal travelFee;
    private BigDecimal hazardSurcharge;
    private BigDecimal subtotal;
    private BigDecimal estimatedTax;
    private BigDecimal totalEstimatedCost;
    private String pricingNotice = "Demo Pricing Notice (INR): Estimates in Indian Rupees are indicative based on standard regional Haryana/NCR fleet rates. Any additional replacement parts, highway tolls, or extra towing require customer approval before charge.";

    public CostEstimateResponseDto() {}

    public CostEstimateResponseDto(BigDecimal baseServiceFee, Double travelDistanceKm, BigDecimal travelFee,
                                   BigDecimal hazardSurcharge, BigDecimal subtotal, BigDecimal estimatedTax,
                                   BigDecimal totalEstimatedCost) {
        this.baseServiceFee = baseServiceFee;
        this.travelDistanceKm = travelDistanceKm;
        this.travelFee = travelFee;
        this.hazardSurcharge = hazardSurcharge;
        this.subtotal = subtotal;
        this.estimatedTax = estimatedTax;
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public BigDecimal getBaseServiceFee() { return baseServiceFee; }
    public void setBaseServiceFee(BigDecimal baseServiceFee) { this.baseServiceFee = baseServiceFee; }

    public Double getTravelDistanceKm() { return travelDistanceKm; }
    public void setTravelDistanceKm(Double travelDistanceKm) { this.travelDistanceKm = travelDistanceKm; }

    public BigDecimal getTravelFee() { return travelFee; }
    public void setTravelFee(BigDecimal travelFee) { this.travelFee = travelFee; }

    public BigDecimal getHazardSurcharge() { return hazardSurcharge; }
    public void setHazardSurcharge(BigDecimal hazardSurcharge) { this.hazardSurcharge = hazardSurcharge; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getEstimatedTax() { return estimatedTax; }
    public void setEstimatedTax(BigDecimal estimatedTax) { this.estimatedTax = estimatedTax; }

    public BigDecimal getTotalEstimatedCost() { return totalEstimatedCost; }
    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) { this.totalEstimatedCost = totalEstimatedCost; }

    public String getPricingNotice() { return pricingNotice; }
    public void setPricingNotice(String pricingNotice) { this.pricingNotice = pricingNotice; }
}
