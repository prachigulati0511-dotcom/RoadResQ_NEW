package com.roadresq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "provider_profiles")
public class ProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @NotBlank
    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @NotBlank
    @Column(name = "service_types", nullable = false, length = 255)
    private String serviceTypes; // comma-separated ServiceType values

    @NotNull
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @Column(nullable = false)
    private Double longitude;

    @Column(name = "is_online")
    private Boolean isOnline = true;

    @Column(nullable = false)
    private Double rating = 4.9;

    @Column(name = "total_reviews")
    private Integer totalReviews = 18;

    @Column(name = "active_vehicle_plate", length = 30)
    private String activeVehiclePlate;

    public ProviderProfile() {}

    public ProviderProfile(User user, String businessName, String serviceTypes, Double latitude, Double longitude, String activeVehiclePlate) {
        this.user = user;
        this.businessName = businessName;
        this.serviceTypes = serviceTypes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOnline = true;
        this.rating = 4.9;
        this.totalReviews = 18;
        this.activeVehiclePlate = activeVehiclePlate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(String serviceTypes) {
        this.serviceTypes = serviceTypes;
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

    public Boolean getIsOnline() {
        return isOnline != null ? isOnline : false;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public String getActiveVehiclePlate() {
        return activeVehiclePlate;
    }

    public void setActiveVehiclePlate(String activeVehiclePlate) {
        this.activeVehiclePlate = activeVehiclePlate;
    }
}
