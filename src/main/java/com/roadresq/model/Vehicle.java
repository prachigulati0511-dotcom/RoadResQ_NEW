package com.roadresq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String make;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String model;

    @NotNull
    @Column(name = "manufacture_year", nullable = false)
    private Integer year;

    @NotBlank
    @Column(name = "license_plate", nullable = false, length = 30)
    private String licensePlate;

    @NotBlank
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private String vehicleType = "Sedan"; // Sedan, SUV, Truck, Motorcycle, EV

    @NotBlank
    @Column(name = "fuel_type", nullable = false, length = 30)
    private String fuelType = "Petrol"; // Petrol, Diesel, Electric, Hybrid

    @Column(length = 30)
    private String color;

    public Vehicle() {}

    public Vehicle(User user, String make, String model, Integer year, String licensePlate, String vehicleType, String fuelType, String color) {
        this.user = user;
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.fuelType = fuelType;
        this.color = color;
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

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
