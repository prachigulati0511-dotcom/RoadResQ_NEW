package com.roadresq.controller;

import com.roadresq.dto.ContactDto;
import com.roadresq.model.User;
import com.roadresq.service.EmergencyContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final EmergencyContactService contactService;

    public ContactController(EmergencyContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<ContactDto>> getContacts(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(contactService.getUserContacts(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> addContact(@AuthenticationPrincipal User user,
                                        @Valid @RequestBody ContactDto dto) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(contactService.addContact(user, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@AuthenticationPrincipal User user,
                                           @PathVariable Long id) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            contactService.deleteContact(user, id);
            return ResponseEntity.ok(Map.of("message", "Contact deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/dispatch/{requestId}")
    public ResponseEntity<?> dispatchEmergencyAlert(@AuthenticationPrincipal User user,
                                                    @PathVariable Long id) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> response = contactService.dispatchEmergencyAlert(user, id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
