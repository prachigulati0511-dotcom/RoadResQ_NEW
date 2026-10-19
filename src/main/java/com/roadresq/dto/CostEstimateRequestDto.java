package com.roadresq.dto;

import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import jakarta.validation.constraints.NotNull;

public class CostEstimateRequestDto {

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    private Severity severity = Severity.NORMAL;
    private Double customerLat;
    private Double customerLon;
    private Double providerLat;
    private Double providerLon;

    public CostEstimateRequestDto() {}

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Double getCustomerLat() { return customerLat; }
    public void setCustomerLat(Double customerLat) { this.customerLat = customerLat; }

    public Double getCustomerLon() { return customerLon; }
    public void setCustomerLon(Double customerLon) { this.customerLon = customerLon; }

    public Double getProviderLat() { return providerLat; }
    public void setProviderLat(Double providerLat) { this.providerLat = providerLat; }

    public Double getProviderLon() { return providerLon; }
    public void setProviderLon(Double providerLon) { this.providerLon = providerLon; }
}
