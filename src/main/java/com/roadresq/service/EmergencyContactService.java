package com.roadresq.service;

import com.roadresq.dto.ContactDto;
import com.roadresq.model.EmergencyContact;
import com.roadresq.model.EmergencyRequest;
import com.roadresq.model.User;
import com.roadresq.repository.EmergencyContactRepository;
import com.roadresq.repository.EmergencyRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository contactRepository;
    private final EmergencyRequestRepository requestRepository;

    public EmergencyContactService(EmergencyContactRepository contactRepository,
                                   EmergencyRequestRepository requestRepository) {
        this.contactRepository = contactRepository;
        this.requestRepository = requestRepository;
    }

    public List<ContactDto> getUserContacts(Long userId) {
        return contactRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContactDto addContact(User user, ContactDto dto) {
        EmergencyContact contact = new EmergencyContact(
                user,
                dto.getName().trim(),
                dto.getPhone().trim(),
                dto.getEmail() != null ? dto.getEmail().trim() : null,
                dto.getRelationship().trim(),
                dto.getNotifyByDefault()
        );
        EmergencyContact saved = contactRepository.save(contact);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteContact(User user, Long contactId) {
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        if (!contact.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized to delete this contact");
        }
        contactRepository.delete(contact);
    }

    /**
     * Prepares dispatched emergency notification payload with customer location and live tracking link
     */
    public Map<String, Object> dispatchEmergencyAlert(User user, Long requestId) {
        EmergencyRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        List<EmergencyContact> contacts = contactRepository.findByUserId(user.getId());

        String trackingUrl = "/index.html?track=" + req.getTrackingCode();
        String alertMessage = String.format(
                "EMERGENCY ALERT from %s: I am experiencing a roadside emergency (%s) near %s. Live Rescue Tracker: %s. Vehicle: %s",
                user.getName(),
                req.getServiceType().getDisplayName(),
                req.getAddress(),
                trackingUrl,
                req.getVehicleInfo() != null ? req.getVehicleInfo() : "Vehicle"
        );

        Map<String, Object> result = new HashMap<>();
        result.put("alertSent", true);
        result.put("contactCount", contacts.size());
        result.put("alertMessage", alertMessage);
        result.put("trackingUrl", trackingUrl);
        result.put("notifiedContacts", contacts.stream().map(c -> c.getName() + " (" + c.getPhone() + ")").collect(Collectors.toList()));

        return result;
    }

    private ContactDto mapToDto(EmergencyContact contact) {
        ContactDto dto = new ContactDto();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setPhone(contact.getPhone());
        dto.setEmail(contact.getEmail());
        dto.setRelationship(contact.getRelationship());
        dto.setNotifyByDefault(contact.getNotifyByDefault());
        return dto;
    }
}
