package com.roadresq.dto;

import java.util.List;

public class DiagnosticRequestDto {
    private String sound;          // clicking, grinding, squeal, silence, knocking, none
    private String visual;         // white_steam, black_smoke, burning_rubber, fuel_smell, fluid_leak, flat_tyre, none
    private String warningLight;   // check_engine, battery_alt, temp_hot, oil_pressure, tpms_tyre, none
    private String engineBehavior; // cranks_wont_start, rapid_clicking, totally_dead, died_while_driving, normal
    private String mobility;       // completely_immobile, unsafe_to_drive, rolling_flat, normal
    private List<String> userAnswers; // Optional free-form questions answers

    public DiagnosticRequestDto() {}

    public String getSound() { return sound; }
    public void setSound(String sound) { this.sound = sound; }

    public String getVisual() { return visual; }
    public void setVisual(String visual) { this.visual = visual; }

    public String getWarningLight() { return warningLight; }
    public void setWarningLight(String warningLight) { this.warningLight = warningLight; }

    public String getEngineBehavior() { return engineBehavior; }
    public void setEngineBehavior(String engineBehavior) { this.engineBehavior = engineBehavior; }

    public String getMobility() { return mobility; }
    public void setMobility(String mobility) { this.mobility = mobility; }

    public List<String> getUserAnswers() { return userAnswers; }
    public void setUserAnswers(List<String> userAnswers) { this.userAnswers = userAnswers; }
}
