package com.roadresq.dto;

import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import java.math.BigDecimal;
import java.util.List;

public class DiagnosticResultDto {
    private String diagnosisTitle;
    private Integer confidencePercent;
    private Severity severity;
    private ServiceType recommendedService;
    private String recommendedServiceName;
    private BigDecimal estimatedBaseCost;
    private List<String> probableCauses;
    private List<String> safetyProtocols;
    private String immediateActionAdvice;

    public DiagnosticResultDto() {}

    public DiagnosticResultDto(String diagnosisTitle, Integer confidencePercent, Severity severity,
                               ServiceType recommendedService, BigDecimal estimatedBaseCost,
                               List<String> probableCauses, List<String> safetyProtocols,
                               String immediateActionAdvice) {
        this.diagnosisTitle = diagnosisTitle;
        this.confidencePercent = confidencePercent;
        this.severity = severity;
        this.recommendedService = recommendedService;
        this.recommendedServiceName = recommendedService != null ? recommendedService.getDisplayName() : "Standard Assistance";
        this.estimatedBaseCost = estimatedBaseCost;
        this.probableCauses = probableCauses;
        this.safetyProtocols = safetyProtocols;
        this.immediateActionAdvice = immediateActionAdvice;
    }

    public String getDiagnosisTitle() { return diagnosisTitle; }
    public void setDiagnosisTitle(String diagnosisTitle) { this.diagnosisTitle = diagnosisTitle; }

    public Integer getConfidencePercent() { return confidencePercent; }
    public void setConfidencePercent(Integer confidencePercent) { this.confidencePercent = confidencePercent; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public ServiceType getRecommendedService() { return recommendedService; }
    public void setRecommendedService(ServiceType recommendedService) {
        this.recommendedService = recommendedService;
        if (recommendedService != null) {
            this.recommendedServiceName = recommendedService.getDisplayName();
        }
    }

    public String getRecommendedServiceName() { return recommendedServiceName; }
    public void setRecommendedServiceName(String recommendedServiceName) { this.recommendedServiceName = recommendedServiceName; }

    public BigDecimal getEstimatedBaseCost() { return estimatedBaseCost; }
    public void setEstimatedBaseCost(BigDecimal estimatedBaseCost) { this.estimatedBaseCost = estimatedBaseCost; }

    public List<String> getProbableCauses() { return probableCauses; }
    public void setProbableCauses(List<String> probableCauses) { this.probableCauses = probableCauses; }

    public List<String> getSafetyProtocols() { return safetyProtocols; }
    public void setSafetyProtocols(List<String> safetyProtocols) { this.safetyProtocols = safetyProtocols; }

    public String getImmediateActionAdvice() { return immediateActionAdvice; }
    public void setImmediateActionAdvice(String immediateActionAdvice) { this.immediateActionAdvice = immediateActionAdvice; }
}
