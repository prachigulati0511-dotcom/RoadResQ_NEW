package com.roadresq.controller;

import com.roadresq.dto.*;
import com.roadresq.model.User;
import com.roadresq.model.Vehicle;
import com.roadresq.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User current = authService.getCurrentUser(user.getEmail());
        return ResponseEntity.ok(current);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody ProfileUpdateRequest request) {
        if (user == null) return ResponseEntity.status(401).build();
        User updated = authService.updateProfile(user, request);
        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "name", updated.getName(),
                "phone", updated.getPhone()
        ));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getUserVehicles(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(authService.getUserVehicles(user.getId()));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<?> addVehicle(@AuthenticationPrincipal User user,
                                        @Valid @RequestBody VehicleDto dto) {
        if (user == null) return ResponseEntity.status(401).build();
        Vehicle vehicle = authService.addVehicle(user, dto);
        return ResponseEntity.ok(vehicle);
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<?> deleteVehicle(@AuthenticationPrincipal User user,
                                           @PathVariable Long id) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            authService.deleteVehicle(user, id);
            return ResponseEntity.ok(Map.of("message", "Vehicle deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
