package com.roadresq.dto;

import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmergencyRequestCreateDto {

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    private Severity severity = Severity.NORMAL;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotBlank(message = "Address or location description is required")
    private String address;

    private String vehicleInfo;
    private String problemDescription;
    private String photoUrl;
    private Boolean consentContacts = false;

    public EmergencyRequestCreateDto() {}

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

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

    public Boolean getConsentContacts() { return consentContacts != null ? consentContacts : false; }
    public void setConsentContacts(Boolean consentContacts) { this.consentContacts = consentContacts; }
}
