package com.roadresq.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileUpdateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String phone;

    public ProfileUpdateRequest() {}

    public ProfileUpdateRequest(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
