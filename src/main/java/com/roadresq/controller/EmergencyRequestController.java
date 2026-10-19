package com.roadresq.controller;

import com.roadresq.dto.*;
import com.roadresq.model.User;
import com.roadresq.service.EmergencyRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class EmergencyRequestController {

    private final EmergencyRequestService requestService;

    public EmergencyRequestController(EmergencyRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<?> createRequest(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody EmergencyRequestCreateDto dto) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        EmergencyRequestResponseDto response = requestService.createRequest(user, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<EmergencyRequestResponseDto>> getMyRequests(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(requestService.getCustomerRequests(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(@AuthenticationPrincipal User user,
                                            @PathVariable Long id) {
        try {
            EmergencyRequestResponseDto dto = requestService.getRequestById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Public tracking endpoint for customer and emergency contacts
     */
    @GetMapping("/track/{trackingCode}")
    public ResponseEntity<?> trackByCode(@PathVariable String trackingCode) {
        try {
            EmergencyRequestResponseDto dto = requestService.getRequestByTrackingCode(trackingCode);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", "No active rescue found for code: " + trackingCode));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@AuthenticationPrincipal User user,
                                          @PathVariable Long id,
                                          @Valid @RequestBody StatusUpdateDto dto) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            EmergencyRequestResponseDto response = requestService.updateStatus(id, user, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/quote/respond")
    public ResponseEntity<?> respondToQuote(@AuthenticationPrincipal User user,
                                            @PathVariable Long id,
                                            @RequestBody Map<String, Boolean> body) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        try {
            EmergencyRequestResponseDto response = requestService.respondToRevisedQuote(id, user, approved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
