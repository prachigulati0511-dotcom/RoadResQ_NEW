package com.roadresq.service;

import com.roadresq.dto.DiagnosticRequestDto;
import com.roadresq.dto.DiagnosticResultDto;
import com.roadresq.model.ServiceType;
import com.roadresq.model.Severity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DiagnosticService {

    private final CostEstimatorService costEstimatorService;

    public DiagnosticService(CostEstimatorService costEstimatorService) {
        this.costEstimatorService = costEstimatorService;
    }

    public DiagnosticResultDto diagnose(DiagnosticRequestDto request) {
        String sound = normalize(request.getSound());
        String visual = normalize(request.getVisual());
        String warning = normalize(request.getWarningLight());
        String engine = normalize(request.getEngineBehavior());
        String mobility = normalize(request.getMobility());

        // Rule 1: Engine Overheating / Steam
        if (visual.contains("steam") || visual.contains("sweet_smell") || warning.contains("temp") || visual.contains("fluid_leak_green")) {
            return new DiagnosticResultDto(
                    "Cooling System Critical Failure / Engine Overheating",
                    95,
                    Severity.CRITICAL_HAZARD,
                    ServiceType.OVERHEATING,
                    costEstimatorService.getBaseFee(ServiceType.OVERHEATING),
                    Arrays.asList(
                            "Pressurized radiator hose rupture or clamp disconnect",
                            "Thermostat valve jammed in closed position",
                            "Water pump impeller mechanical failure or belt snap",
                            "Radiator core cooling fan relay blown"
                    ),
                    Arrays.asList(
                            "WARNING: NEVER open the radiator cap or coolant expansion tank while engine is hot. Scalding steam will cause severe bodily burns.",
                            "Shut off engine immediately to prevent catastrophic cylinder head warping or engine seizure.",
                            "Exit vehicle and move behind the highway safety barrier while awaiting rescue."
                    ),
                    "Keep bonnet closed if active steam is rising. Professional coolant pressure test and mobile refill required."
            );
        }

        // Rule 2: Flat Tyre / Wheel Damage
        if (visual.contains("flat") || visual.contains("shredded") || warning.contains("tpms") || mobility.contains("flat") || sound.contains("thump")) {
            return new DiagnosticResultDto(
                    "Tyre Puncture or Pressure Decompression",
                    98,
                    Severity.URGENT,
                    ServiceType.FLAT_TYRE,
                    costEstimatorService.getBaseFee(ServiceType.FLAT_TYRE),
                    Arrays.asList(
                            "Foreign object tread penetration (nail, screw, road debris)",
                            "Alloy wheel rim flange bending causing bead seal leak",
                            "Valve stem core failure or high-speed blowout"
                    ),
                    Arrays.asList(
                            "DO NOT continue driving on a deflated tyre; rolling on the rim will crack the wheel and permanently ruin the tyre sidewall.",
                            "Turn on hazard warning flashers and park on flat, firm ground away from live traffic lanes.",
                            "Engage handbrake and leave vehicle in Gear (or 'P' for Automatic)."
                    ),
                    "Mobile technician will perform on-site vulcanized plug repair or mount your vehicle's spare wheel."
            );
        }

        // Rule 3: Battery Discharge / Alternator Failure
        if (engine.contains("clicking") || engine.contains("dead") || warning.contains("battery") || sound.contains("clicking") || sound.contains("silence")) {
            return new DiagnosticResultDto(
                    "Electrical Power System Failure / Battery Depletion",
                    94,
                    Severity.NORMAL,
                    ServiceType.BATTERY_JUMP,
                    costEstimatorService.getBaseFee(ServiceType.BATTERY_JUMP),
                    Arrays.asList(
                            "12V Lead-Acid or AGM battery cold-cranking amps (CCA) depleted",
                            "Alternator diode failure unable to sustain vehicle electrical load",
                            "Parasitic battery drain or oxidized terminal contacts"
                    ),
                    Arrays.asList(
                            "Turn off all interior accessories (air conditioning, headlights, audio display) to conserve remaining voltage.",
                            "Avoid repeated continuous ignition attempts as this can burn out the starter motor solenoid.",
                            "Remain inside your vehicle with doors locked if parked in an unlit area."
                    ),
                    "Rescue van equipped with high-amp commercial booster and computerized alternator voltage analyzer dispatched."
            );
        }

        // Rule 4: Fuel Starvation / Sputter
        if (visual.contains("fuel") || engine.contains("sputter") || engine.contains("died_while")) {
            return new DiagnosticResultDto(
                    "Fuel Supply Exhaustion or Delivery Starvation",
                    90,
                    Severity.URGENT,
                    ServiceType.FUEL_DELIVERY,
                    costEstimatorService.getBaseFee(ServiceType.FUEL_DELIVERY),
                    Arrays.asList(
                            "Empty fuel tank or inaccurate fuel float sensor reading",
                            "In-tank electric fuel pump cavitation or pump relay cutoff",
                            "Severe fuel filter vapor lock or sediment blockage"
                    ),
                    Arrays.asList(
                            "STRICT SAFETY: Absolutely no smoking, vapes, or open ignition sources near vehicle.",
                            "Keep your hazard lights on and vehicle windows closed if fuel vapor aroma is strong.",
                            "Ensure vehicle is completely powered off."
                    ),
                    "Mobile emergency fuel unit dispatched with 10L clean certified fuel (Petrol/Diesel based on vehicle spec)."
            );
        }

        // Rule 5: Severe Mechanical / Transmission / Towing
        if (sound.contains("grind") || sound.contains("knock") || mobility.contains("immobile") || warning.contains("oil")) {
            return new DiagnosticResultDto(
                    "Heavy Mechanical Drive-Train Failure (Towing Recommended)",
                    92,
                    Severity.CRITICAL_HAZARD,
                    ServiceType.TOWING,
                    costEstimatorService.getBaseFee(ServiceType.TOWING),
                    Arrays.asList(
                            "Transmission clutch pack failure, torque converter stall, or drive axle fracture",
                            "Engine mechanical seizure or critical oil pressure loss",
                            "Suspension control arm or tie-rod structural breakage"
                    ),
                    Arrays.asList(
                            "Vehicle cannot be safely driven under its own power.",
                            "Engage emergency handbrake firmly. If on a grade, turn wheels toward curb.",
                            "Exit vehicle through passenger side door and wait at a safe distance behind road barriers."
                    ),
                    "Certified hydraulic flatbed carrier assigned for zero-damage vehicle transit to authorized service center."
            );
        }

        // Default: General Mechanical Breakdown Assistance
        return new DiagnosticResultDto(
            "General Engine & Powertrain Diagnostic Needed",
            85,
            Severity.NORMAL,
            ServiceType.ENGINE_BREAKDOWN,
            costEstimatorService.getBaseFee(ServiceType.ENGINE_BREAKDOWN),
            Arrays.asList(
                    "Electronic ignition or sensor communication fault (Crankshaft/Camshaft Position sensor)",
                    "Air intake flow mass discrepancy or throttle actuator freeze",
                    "Security immobilizer / key transponder handshake timeout"
            ),
            Arrays.asList(
                    "Safely park vehicle clear of active traffic flow.",
                    "Keep emergency hazard lights illuminated.",
                    "Have your vehicle key and registration documents accessible for the service technician."
            ),
            "Diagnostic technician equipped with OBD-II scanner and emergency roadside kit dispatched to assess on-site."
        );
    }

    private String normalize(String str) {
        return str == null ? "" : str.trim().toLowerCase();
    }
}
