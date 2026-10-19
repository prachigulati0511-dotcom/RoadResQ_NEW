package com.roadresq.service;

import com.roadresq.dto.*;
import com.roadresq.model.*;
import com.roadresq.repository.EmergencyRequestRepository;
import com.roadresq.repository.ProviderProfileRepository;
import com.roadresq.util.GeoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmergencyRequestService {

    private final EmergencyRequestRepository requestRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final CostEstimatorService costEstimatorService;
    private final SecureRandom random = new SecureRandom();

    public EmergencyRequestService(EmergencyRequestRepository requestRepository,
                                   ProviderProfileRepository providerProfileRepository,
                                   CostEstimatorService costEstimatorService) {
        this.requestRepository = requestRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.costEstimatorService = costEstimatorService;
    }

    @Transactional
    public EmergencyRequestResponseDto createRequest(User customer, EmergencyRequestCreateDto dto) {
        // Generate human-friendly unique tracking code: RR-IN-YYMMDD-XXXX
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int randNum = 1000 + random.nextInt(9000);
        String trackingCode = "RR-IN-" + dateStr + "-" + randNum;

        // Calculate itemized transparent cost
        CostEstimateRequestDto costReq = new CostEstimateRequestDto();
        costReq.setServiceType(dto.getServiceType());
        costReq.setSeverity(dto.getSeverity());
        costReq.setCustomerLat(dto.getLatitude());
        costReq.setCustomerLon(dto.getLongitude());

        CostEstimateResponseDto estimate = costEstimatorService.calculateEstimate(costReq);

        EmergencyRequest request = new EmergencyRequest();
        request.setTrackingCode(trackingCode);
        request.setCustomer(customer);
        request.setServiceType(dto.getServiceType());
        request.setSeverity(dto.getSeverity() != null ? dto.getSeverity() : Severity.NORMAL);
        request.setStatus(RequestStatus.PENDING);
        request.setLatitude(dto.getLatitude());
        request.setLongitude(dto.getLongitude());
        request.setAddress(dto.getAddress());
        request.setVehicleInfo(dto.getVehicleInfo());
        request.setProblemDescription(dto.getProblemDescription());
        request.setPhotoUrl(dto.getPhotoUrl());
        request.setConsentContacts(dto.getConsentContacts());

        // Pricing breakdown
        request.setBaseFee(estimate.getBaseServiceFee());
        request.setTravelFee(estimate.getTravelFee());
        request.setHazardFee(estimate.getHazardSurcharge());
        request.setTotalEstimatedCost(estimate.getTotalEstimatedCost());
        request.setQuoteStatus(QuoteStatus.ORIGINAL);
        request.setCreatedAt(LocalDateTime.now());

        EmergencyRequest saved = requestRepository.save(request);
        return mapToResponseDto(saved);
    }

    /**
     * ATOMIC FIRST-PROVIDER-WINS ASSIGNMENT
     * Uses pessimistic locking (SELECT FOR UPDATE) and transaction isolation
     * to eliminate race conditions between simultaneous acceptances.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EmergencyRequestResponseDto acceptRequest(Long requestId, User providerUser) {
        EmergencyRequest request;
        try {
            // Step 1: Acquire row-level lock
            request = requestRepository.findByIdForUpdate(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        } catch (org.springframework.dao.ConcurrencyFailureException | jakarta.persistence.PessimisticLockException e) {
            throw new IllegalStateException("Conflict: This request has already been assigned to another provider.");
        }

        // Step 2: Strict check - first provider wins!
        if (request.getStatus() != RequestStatus.PENDING || request.getAssignedProvider() != null) {
            throw new IllegalStateException("Conflict: This request has already been assigned to another provider.");
        }

        // Step 3: Assign and transition status
        request.setAssignedProvider(providerUser);
        request.setStatus(RequestStatus.ACCEPTED);
        request.setAssignedAt(LocalDateTime.now());

        EmergencyRequest saved = requestRepository.save(request);
        return mapToResponseDto(saved);
    }

    @Transactional
    public EmergencyRequestResponseDto updateStatus(Long requestId, User user, StatusUpdateDto dto) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Validate user authorization (must be assigned provider, customer for cancel, or admin)
        boolean isAssignedProvider = request.getAssignedProvider() != null &&
                request.getAssignedProvider().getId().equals(user.getId());
        boolean isCustomer = request.getCustomer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAssignedProvider && !isCustomer && !isAdmin) {
            throw new IllegalStateException("Unauthorized to update this request status");
        }

        if (dto.getStatus() == RequestStatus.CANCELLED) {
            if (request.getStatus() == RequestStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel an already completed request");
            }
            request.setStatus(RequestStatus.CANCELLED);
            request.setCancelReason(dto.getCancelReason() != null ? dto.getCancelReason() : "Cancelled by user");
        } else {
            if (!isAssignedProvider && !isAdmin) {
                throw new IllegalStateException("Only assigned provider can advance service status");
            }
            request.setStatus(dto.getStatus());
            if (dto.getStatus() == RequestStatus.COMPLETED) {
                request.setCompletedAt(LocalDateTime.now());
            }
        }

        EmergencyRequest saved = requestRepository.save(request);
        return mapToResponseDto(saved);
    }

    @Transactional
    public EmergencyRequestResponseDto submitRevisedQuote(Long requestId, User providerUser, QuoteDto dto) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getAssignedProvider() == null || !request.getAssignedProvider().getId().equals(providerUser.getId())) {
            throw new IllegalStateException("Only the assigned provider can submit a revised quote");
        }

        request.setRevisedQuote(dto.getRevisedQuote());
        request.setRevisedQuoteReason(dto.getReason());
        request.setQuoteStatus(QuoteStatus.REVISED_PENDING);

        EmergencyRequest saved = requestRepository.save(request);
        return mapToResponseDto(saved);
    }

    @Transactional
    public EmergencyRequestResponseDto respondToRevisedQuote(Long requestId, User customerUser, boolean approved) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!request.getCustomer().getId().equals(customerUser.getId())) {
            throw new IllegalStateException("Only the customer can approve or decline a revised quote");
        }

        if (request.getQuoteStatus() != QuoteStatus.REVISED_PENDING) {
            throw new IllegalStateException("No pending revised quote to respond to");
        }

        if (approved) {
            request.setQuoteStatus(QuoteStatus.REVISED_APPROVED);
            request.setTotalEstimatedCost(request.getRevisedQuote());
        } else {
            request.setQuoteStatus(QuoteStatus.REVISED_DECLINED);
        }

        EmergencyRequest saved = requestRepository.save(request);
        return mapToResponseDto(saved);
    }

    public EmergencyRequestResponseDto getRequestById(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        return mapToResponseDto(request);
    }

    public EmergencyRequestResponseDto getRequestByTrackingCode(String trackingCode) {
        EmergencyRequest request = requestRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new IllegalArgumentException("Request not found for tracking code: " + trackingCode));
        return mapToResponseDto(request);
    }

    public List<EmergencyRequestResponseDto> getCustomerRequests(Long customerId) {
        return requestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<EmergencyRequestResponseDto> getPendingRequests() {
        return requestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<EmergencyRequestResponseDto> getProviderRequests(Long providerId) {
        return requestRepository.findByAssignedProviderIdOrderByCreatedAtDesc(providerId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public EmergencyRequestResponseDto mapToResponseDto(EmergencyRequest req) {
        EmergencyRequestResponseDto dto = new EmergencyRequestResponseDto();
        dto.setId(req.getId());
        dto.setTrackingCode(req.getTrackingCode());
        dto.setStatus(req.getStatus());
        dto.setServiceType(req.getServiceType());
        dto.setSeverity(req.getSeverity());
        dto.setLatitude(req.getLatitude());
        dto.setLongitude(req.getLongitude());
        dto.setAddress(req.getAddress());
        dto.setVehicleInfo(req.getVehicleInfo());
        dto.setProblemDescription(req.getProblemDescription());
        dto.setPhotoUrl(req.getPhotoUrl());

        dto.setBaseFee(req.getBaseFee());
        dto.setTravelFee(req.getTravelFee());
        dto.setHazardFee(req.getHazardFee());
        dto.setTotalEstimatedCost(req.getTotalEstimatedCost());
        dto.setRevisedQuote(req.getRevisedQuote());
        dto.setRevisedQuoteReason(req.getRevisedQuoteReason());
        dto.setQuoteStatus(req.getQuoteStatus());

        dto.setCustomerId(req.getCustomer().getId());
        dto.setCustomerName(req.getCustomer().getName());
        dto.setCustomerPhone(req.getCustomer().getPhone());
        dto.setCustomerEmail(req.getCustomer().getEmail());

        if (req.getAssignedProvider() != null) {
            User p = req.getAssignedProvider();
            dto.setProviderId(p.getId());
            dto.setProviderName(p.getName());
            dto.setProviderPhone(p.getPhone());

            Optional<ProviderProfile> profOpt = providerProfileRepository.findByUserId(p.getId());
            if (profOpt.isPresent()) {
                ProviderProfile prof = profOpt.get();
                dto.setProviderBusinessName(prof.getBusinessName());
                dto.setProviderRating(prof.getRating());
                dto.setProviderVehiclePlate(prof.getActiveVehiclePlate());
                dto.setProviderLatitude(prof.getLatitude());
                dto.setProviderLongitude(prof.getLongitude());

                double distance = GeoUtils.calculateDistanceKm(
                        req.getLatitude(), req.getLongitude(),
                        prof.getLatitude(), prof.getLongitude()
                );
                dto.setDistanceKm(distance);
                dto.setEtaMinutes(GeoUtils.calculateEtaMinutes(distance));
            }
        }

        dto.setConsentContacts(req.getConsentContacts());
        dto.setCancelReason(req.getCancelReason());
        dto.setCreatedAt(req.getCreatedAt());
        dto.setAssignedAt(req.getAssignedAt());
        dto.setCompletedAt(req.getCompletedAt());

        return dto;
    }
}
