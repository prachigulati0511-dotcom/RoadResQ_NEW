package com.roadresq.service;

import com.roadresq.dto.DiagnosticRequestDto;
import com.roadresq.dto.DiagnosticResultDto;
import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiagnosticServiceTest {

    private final CostEstimatorService costEstimatorService = new CostEstimatorService();
    private final DiagnosticService diagnosticService = new DiagnosticService(costEstimatorService);

    @Test
    @DisplayName("Overheating symptoms trigger OVERHEATING service and CRITICAL_HAZARD severity")
    void testOverheatingDiagnosis() {
        DiagnosticRequestDto req = new DiagnosticRequestDto();
        req.setVisual("white_steam");
        req.setWarningLight("temp_hot");

        DiagnosticResultDto result = diagnosticService.diagnose(req);
        assertEquals(ServiceType.OVERHEATING, result.getRecommendedService());
        assertEquals(Severity.CRITICAL_HAZARD, result.getSeverity());
        assertTrue(result.getSafetyProtocols().stream().anyMatch(s -> s.contains("NEVER open the radiator cap")));
    }

    @Test
    @DisplayName("Flat tyre symptoms trigger FLAT_TYRE service and URGENT severity")
    void testFlatTyreDiagnosis() {
        DiagnosticRequestDto req = new DiagnosticRequestDto();
        req.setVisual("flat_tyre");
        req.setMobility("rolling_flat");

        DiagnosticResultDto result = diagnosticService.diagnose(req);
        assertEquals(ServiceType.FLAT_TYRE, result.getRecommendedService());
        assertEquals(Severity.URGENT, result.getSeverity());
        assertTrue(result.getImmediateActionAdvice().contains("plug repair") || result.getImmediateActionAdvice().contains("spare wheel"));
    }

    @Test
    @DisplayName("Rapid clicking and battery lamp trigger BATTERY_JUMP service")
    void testBatteryJumpDiagnosis() {
        DiagnosticRequestDto req = new DiagnosticRequestDto();
        req.setEngineBehavior("rapid_clicking");
        req.setWarningLight("battery_alt");

        DiagnosticResultDto result = diagnosticService.diagnose(req);
        assertEquals(ServiceType.BATTERY_JUMP, result.getRecommendedService());
        assertNotNull(result.getEstimatedBaseCost());
    }
}
