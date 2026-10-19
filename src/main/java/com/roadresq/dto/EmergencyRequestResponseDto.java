package com.roadresq.dto;

import com.roadresq.model.QuoteStatus;
import com.roadresq.model.RequestStatus;
import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmergencyRequestResponseDto {

    private Long id;
    private String trackingCode;
    private RequestStatus status;
    private ServiceType serviceType;
    private String serviceTypeDisplay;
    private Severity severity;
    private Double latitude;
    private Double longitude;
    private String address;
    private String vehicleInfo;
    private String problemDescription;
    private String photoUrl;

    // Pricing
    private BigDecimal baseFee;
    private BigDecimal travelFee;
    private BigDecimal hazardFee;
    private BigDecimal totalEstimatedCost;
    private BigDecimal revisedQuote;
    private String revisedQuoteReason;
    private QuoteStatus quoteStatus;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Provider info (if assigned)
    private Long providerId;
    private String providerName;
    private String providerBusinessName;
    private String providerPhone;
    private Double providerRating;
    private String providerVehiclePlate;
    private Double providerLatitude;
    private Double providerLongitude;
    private Double distanceKm;
    private Integer etaMinutes;

    private Boolean consentContacts;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    public EmergencyRequestResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        if (serviceType != null) {
            this.serviceTypeDisplay = serviceType.getDisplayName();
        }
    }

    public String getServiceTypeDisplay() { return serviceTypeDisplay; }
    public void setServiceTypeDisplay(String serviceTypeDisplay) { this.serviceTypeDisplay = serviceTypeDisplay; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public BigDecimal getBaseFee() { return baseFee; }
    public void setBaseFee(BigDecimal baseFee) { this.baseFee = baseFee; }

    public BigDecimal getTravelFee() { return travelFee; }
    public void setTravelFee(BigDecimal travelFee) { this.travelFee = travelFee; }

    public BigDecimal getHazardFee() { return hazardFee; }
    public void setHazardFee(BigDecimal hazardFee) { this.hazardFee = hazardFee; }

    public BigDecimal getTotalEstimatedCost() { return totalEstimatedCost; }
    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) { this.totalEstimatedCost = totalEstimatedCost; }

    public BigDecimal getRevisedQuote() { return revisedQuote; }
    public void setRevisedQuote(BigDecimal revisedQuote) { this.revisedQuote = revisedQuote; }

    public String getRevisedQuoteReason() { return revisedQuoteReason; }
    public void setRevisedQuoteReason(String revisedQuoteReason) { this.revisedQuoteReason = revisedQuoteReason; }

    public QuoteStatus getQuoteStatus() { return quoteStatus; }
    public void setQuoteStatus(QuoteStatus quoteStatus) { this.quoteStatus = quoteStatus; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderBusinessName() { return providerBusinessName; }
    public void setProviderBusinessName(String providerBusinessName) { this.providerBusinessName = providerBusinessName; }

    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }

    public Double getProviderRating() { return providerRating; }
    public void setProviderRating(Double providerRating) { this.providerRating = providerRating; }

    public String getProviderVehiclePlate() { return providerVehiclePlate; }
    public void setProviderVehiclePlate(String providerVehiclePlate) { this.providerVehiclePlate = providerVehiclePlate; }

    public Double getProviderLatitude() { return providerLatitude; }
    public void setProviderLatitude(Double providerLatitude) { this.providerLatitude = providerLatitude; }

    public Double getProviderLongitude() { return providerLongitude; }
    public void setProviderLongitude(Double providerLongitude) { this.providerLongitude = providerLongitude; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }

    public Boolean getConsentContacts() { return consentContacts; }
    public void setConsentContacts(Boolean consentContacts) { this.consentContacts = consentContacts; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
