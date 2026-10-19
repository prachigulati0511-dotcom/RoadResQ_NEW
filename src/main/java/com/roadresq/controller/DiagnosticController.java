package com.roadresq.controller;

import com.roadresq.dto.DiagnosticRequestDto;
import com.roadresq.dto.DiagnosticResultDto;
import com.roadresq.service.DiagnosticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class DiagnosticController {

    private final DiagnosticService diagnosticService;

    public DiagnosticController(DiagnosticService diagnosticService) {
        this.diagnosticService = diagnosticService;
    }

    @PostMapping("/diagnose")
    public ResponseEntity<DiagnosticResultDto> diagnoseProblem(@RequestBody DiagnosticRequestDto request) {
        DiagnosticResultDto result = diagnosticService.diagnose(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/symptoms")
    public ResponseEntity<Map<String, Object>> getSymptomOptions() {
        return ResponseEntity.ok(Map.of(
                "sounds", List.of(
                        Map.of("id", "clicking", "label", "Rapid clicking or buzzing when turning key", "icon", "volume-2"),
                        Map.of("id", "grinding", "label", "Loud metal grinding or scraping", "icon", "disc"),
                        Map.of("id", "squeal", "label", "High pitched belt squealing or whining", "icon", "activity"),
                        Map.of("id", "knocking", "label", "Heavy rhythmic engine knocking or thumping", "icon", "alert-circle"),
                        Map.of("id", "silence", "label", "Complete silence / no starter engagement", "icon", "volume-x"),
                        Map.of("id", "none", "label", "No abnormal sounds", "icon", "check")
                ),
                "visuals", List.of(
                        Map.of("id", "white_steam", "label", "White steam billows from radiator/bonnet", "badge", "Critical"),
                        Map.of("id", "flat_tyre", "label", "Visible tyre deflation, puncture, or rim drop", "badge", "Urgent"),
                        Map.of("id", "sweet_smell", "label", "Sweet syrup aroma or green coolant puddle", "badge", "Warning"),
                        Map.of("id", "fluid_leak", "label", "Dark engine oil puddle beneath chassis", "badge", "Warning"),
                        Map.of("id", "burning_rubber", "label", "Acrid burning rubber or electrical odor", "badge", "Hazard"),
                        Map.of("id", "fuel_smell", "label", "Strong unburned petrol/diesel fumes", "badge", "Hazard"),
                        Map.of("id", "none", "label", "No visual leaks, odors, or smoke", "badge", "Normal")
                ),
                "warnings", List.of(
                        Map.of("id", "battery_alt", "label", "Battery / Alternator warning lamp (Red)", "urgency", "High"),
                        Map.of("id", "temp_hot", "label", "Coolant Temperature high / Overheat lamp", "urgency", "Critical"),
                        Map.of("id", "tpms_tyre", "label", "Tyre Pressure Monitoring (TPMS) symbol", "urgency", "Medium"),
                        Map.of("id", "oil_pressure", "label", "Oil pressure / Can symbol (Red)", "urgency", "Critical"),
                        Map.of("id", "check_engine", "label", "Check Engine Light (Amber MIL)", "urgency", "Medium"),
                        Map.of("id", "none", "label", "No warning lights active", "urgency", "None")
                ),
                "mobility", List.of(
                        Map.of("id", "completely_immobile", "label", "Engine will not start; car completely stranded"),
                        Map.of("id", "rolling_flat", "label", "Engine runs but tyre flat; cannot safely roll"),
                        Map.of("id", "unsafe_to_drive", "label", "Severe shuddering / dangerous to merge into traffic"),
                        Map.of("id", "normal", "label", "Vehicle can safely crawl to shoulder")
                )
        ));
    }
}
