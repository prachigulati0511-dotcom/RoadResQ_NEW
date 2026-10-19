package com.roadresq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_requests")
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_code", nullable = false, unique = true, length = 50)
    private String trackingCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_provider_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User assignedProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 50)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity = Severity.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status = RequestStatus.PENDING;

    @NotNull
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @Column(nullable = false)
    private Double longitude;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "vehicle_info", length = 255)
    private String vehicleInfo;

    @Column(name = "problem_description", columnDefinition = "TEXT")
    private String problemDescription;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @NotNull
    @Column(name = "base_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFee;

    @NotNull
    @Column(name = "travel_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal travelFee;

    @Column(name = "hazard_fee", precision = 10, scale = 2)
    private BigDecimal hazardFee = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_estimated_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEstimatedCost;

    @Column(name = "revised_quote", precision = 10, scale = 2)
    private BigDecimal revisedQuote;

    @Column(name = "revised_quote_reason", length = 255)
    private String revisedQuoteReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_status", length = 30)
    private QuoteStatus quoteStatus = QuoteStatus.ORIGINAL;

    @Column(name = "consent_contacts")
    private Boolean consentContacts = false;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Long version;

    public EmergencyRequest() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getAssignedProvider() {
        return assignedProvider;
    }

    public void setAssignedProvider(User assignedProvider) {
        this.assignedProvider = assignedProvider;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee;
    }

    public BigDecimal getTravelFee() {
        return travelFee;
    }

    public void setTravelFee(BigDecimal travelFee) {
        this.travelFee = travelFee;
    }

    public BigDecimal getHazardFee() {
        return hazardFee != null ? hazardFee : BigDecimal.ZERO;
    }

    public void setHazardFee(BigDecimal hazardFee) {
        this.hazardFee = hazardFee;
    }

    public BigDecimal getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public BigDecimal getRevisedQuote() {
        return revisedQuote;
    }

    public void setRevisedQuote(BigDecimal revisedQuote) {
        this.revisedQuote = revisedQuote;
    }

    public String getRevisedQuoteReason() {
        return revisedQuoteReason;
    }

    public void setRevisedQuoteReason(String revisedQuoteReason) {
        this.revisedQuoteReason = revisedQuoteReason;
    }

    public QuoteStatus getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(QuoteStatus quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public Boolean getConsentContacts() {
        return consentContacts != null ? consentContacts : false;
    }

    public void setConsentContacts(Boolean consentContacts) {
        this.consentContacts = consentContacts;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
