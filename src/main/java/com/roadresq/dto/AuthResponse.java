package com.roadresq.dto;

import com.roadresq.model.Role;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Object profile; // ProviderProfile if provider
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String name, String email, String phone, Role role, Object profile, String message) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.profile = profile;
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Object getProfile() { return profile; }
    public void setProfile(Object profile) { this.profile = profile; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
