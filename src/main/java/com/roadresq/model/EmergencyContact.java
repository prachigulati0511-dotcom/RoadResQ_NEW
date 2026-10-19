package com.roadresq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 100)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String relationship;

    @Column(name = "notify_by_default")
    private Boolean notifyByDefault = true;

    public EmergencyContact() {}

    public EmergencyContact(User user, String name, String phone, String email, String relationship, Boolean notifyByDefault) {
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.relationship = relationship;
        this.notifyByDefault = notifyByDefault != null ? notifyByDefault : true;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Boolean getNotifyByDefault() {
        return notifyByDefault != null ? notifyByDefault : true;
    }

    public void setNotifyByDefault(Boolean notifyByDefault) {
        this.notifyByDefault = notifyByDefault;
    }
}
