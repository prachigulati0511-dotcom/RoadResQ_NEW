package com.roadresq.model;

public enum ServiceType {
    FLAT_TYRE("Flat Tyre / Spare Replacement"),
    BATTERY_JUMP("Battery Jumpstart / Test"),
    FUEL_DELIVERY("Emergency Fuel Delivery"),
    TOWING("Flatbed Towing Service"),
    ENGINE_BREAKDOWN("Engine / Mechanical Breakdown"),
    OVERHEATING("Engine Overheating / Coolant"),
    LOCKOUT("Vehicle Lockout Assistance"),
    EV_CHARGE("Mobile EV Charging Unit"),
    ACCIDENT_ASSISTANCE("Accident Recovery & Assistance"),
    OTHER("Other Roadside Emergency");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
