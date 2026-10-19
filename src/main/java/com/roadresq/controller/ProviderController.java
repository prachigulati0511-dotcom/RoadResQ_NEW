package com.roadresq.controller;

import com.roadresq.dto.EmergencyRequestResponseDto;
import com.roadresq.dto.QuoteDto;
import com.roadresq.dto.StatusUpdateDto;
import com.roadresq.model.Role;
import com.roadresq.model.User;
import com.roadresq.service.EmergencyRequestService;
import com.roadresq.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    private final ProviderService providerService;
    private final EmergencyRequestService requestService;

    public ProviderController(ProviderService providerService,
                              EmergencyRequestService requestService) {
        this.providerService = providerService;
        this.requestService = requestService;
    }

    /**
     * Public / Live map radar of nearby available providers
     */
    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableProviders(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        return ResponseEntity.ok(providerService.getAvailableProviders(lat, lon));
    }

    /**
     * Broadcast pool of incoming unassigned emergency requests for online providers
     */
    @GetMapping("/pending-requests")
    public ResponseEntity<List<EmergencyRequestResponseDto>> getPendingRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    /**
     * Active & completed jobs for the authenticated provider
     */
    @GetMapping("/my-jobs")
    public ResponseEntity<List<EmergencyRequestResponseDto>> getMyJobs(@AuthenticationPrincipal User user) {
        if (user == null || user.getRole() != Role.PROVIDER) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(requestService.getProviderRequests(user.getId()));
    }

    /**
     * ATOMIC FIRST-PROVIDER-WINS ACCEPTANCE ENDPOINT
     * Returns HTTP 200 on success.
     * Returns HTTP 409 Conflict if another provider accepted this request first.
     */
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<?> acceptRequest(@AuthenticationPrincipal User user,
                                           @PathVariable Long id) {
        if (user == null || user.getRole() != Role.PROVIDER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only certified providers can accept jobs"));
        }

        try {
            EmergencyRequestResponseDto assigned = requestService.acceptRequest(id, user);
            return ResponseEntity.ok(assigned);
        } catch (IllegalStateException e) {
            // First-Provider-Wins conflict detection
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "conflict", true,
                    "error", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Provider updates emergency request status (ON_THE_WAY, ARRIVED, IN_PROGRESS, COMPLETED)
     */
    @PatchMapping("/requests/{id}/status")
    public ResponseEntity<?> updateJobStatus(@AuthenticationPrincipal User user,
                                             @PathVariable Long id,
                                             @Valid @RequestBody StatusUpdateDto dto) {
        if (user == null || user.getRole() != Role.PROVIDER) {
            return ResponseEntity.status(403).build();
        }
        try {
            EmergencyRequestResponseDto updated = requestService.updateStatus(id, user, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Provider submits a transparent revised quote if on-site diagnostics require additional parts
     */
    @PostMapping("/requests/{id}/quote")
    public ResponseEntity<?> submitRevisedQuote(@AuthenticationPrincipal User user,
                                                @PathVariable Long id,
                                                @Valid @RequestBody QuoteDto dto) {
        if (user == null || user.getRole() != Role.PROVIDER) {
            return ResponseEntity.status(403).build();
        }
        try {
            EmergencyRequestResponseDto updated = requestService.submitRevisedQuote(id, user, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Toggle online availability status
     */
    @PostMapping("/status")
    public ResponseEntity<?> toggleStatus(@AuthenticationPrincipal User user,
                                          @RequestBody Map<String, Boolean> body) {
        if (user == null || user.getRole() != Role.PROVIDER) {
            return ResponseEntity.status(403).build();
        }
        boolean isOnline = Boolean.TRUE.equals(body.get("isOnline"));
        return ResponseEntity.ok(providerService.toggleOnlineStatus(user, isOnline));
    }

    /**
     * Update provider GPS coordinates
     */
    @PostMapping("/location")
    public ResponseEntity<?> updateLocation(@AuthenticationPrincipal User user,
                                            @RequestBody Map<String, Double> coords) {
        if (user == null || user.getRole() != Role.PROVIDER) {
            return ResponseEntity.status(403).build();
        }
        Double lat = coords.get("latitude");
        Double lon = coords.get("longitude");
        if (lat == null || lon == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "latitude and longitude required"));
        }
        return ResponseEntity.ok(providerService.updateLocation(user, lat, lon));
    }
}
