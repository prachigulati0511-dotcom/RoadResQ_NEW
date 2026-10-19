package com.roadresq.dto;

import jakarta.validation.constraints.NotBlank;

public class ContactDto {
    private Long id;

    @NotBlank(message = "Contact name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;

    @NotBlank(message = "Relationship is required")
    private String relationship;

    private Boolean notifyByDefault = true;

    public ContactDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public Boolean getNotifyByDefault() { return notifyByDefault != null ? notifyByDefault : true; }
    public void setNotifyByDefault(Boolean notifyByDefault) { this.notifyByDefault = notifyByDefault; }
}
