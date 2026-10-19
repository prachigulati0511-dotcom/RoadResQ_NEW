package com.roadresq.service;

import com.roadresq.dto.ReviewDto;
import com.roadresq.model.*;
import com.roadresq.repository.EmergencyRequestRepository;
import com.roadresq.repository.ProviderProfileRepository;
import com.roadresq.repository.RatingReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final RatingReviewRepository reviewRepository;
    private final EmergencyRequestRepository requestRepository;
    private final ProviderProfileRepository providerProfileRepository;

    public ReviewService(RatingReviewRepository reviewRepository,
                         EmergencyRequestRepository requestRepository,
                         ProviderProfileRepository providerProfileRepository) {
        this.reviewRepository = reviewRepository;
        this.requestRepository = requestRepository;
        this.providerProfileRepository = providerProfileRepository;
    }

    @Transactional
    public ReviewDto submitReview(User customer, ReviewDto dto) {
        EmergencyRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalStateException("Only the customer of this request can submit a review");
        }

        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new IllegalStateException("Reviews can only be submitted for completed requests");
        }

        if (request.getAssignedProvider() == null) {
            throw new IllegalStateException("No provider was assigned to this request");
        }

        Optional<RatingReview> existing = reviewRepository.findByRequestId(request.getId());
        if (existing.isPresent()) {
            throw new IllegalStateException("A review has already been submitted for this request");
        }

        User provider = request.getAssignedProvider();
        RatingReview review = new RatingReview(
                request,
                customer,
                provider,
                dto.getStars(),
                dto.getTags(),
                dto.getComment()
        );

        RatingReview saved = reviewRepository.save(review);

        // Update provider's overall rating score
        Optional<ProviderProfile> profOpt = providerProfileRepository.findByUserId(provider.getId());
        if (profOpt.isPresent()) {
            ProviderProfile prof = profOpt.get();
            int currentTotal = prof.getTotalReviews() != null ? prof.getTotalReviews() : 0;
            double currentRating = prof.getRating() != null ? prof.getRating() : 5.0;

            double newRating = ((currentRating * currentTotal) + dto.getStars()) / (currentTotal + 1);
            prof.setRating(Math.round(newRating * 10.0) / 10.0);
            prof.setTotalReviews(currentTotal + 1);
            providerProfileRepository.save(prof);
        }

        return mapToDto(saved);
    }

    public List<ReviewDto> getProviderReviews(Long providerId) {
        return reviewRepository.findByProviderIdOrderByCreatedAtDesc(providerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ReviewDto> getReviewForRequest(Long requestId) {
        return reviewRepository.findByRequestId(requestId).map(this::mapToDto);
    }

    private ReviewDto mapToDto(RatingReview review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setRequestId(review.getRequest().getId());
        dto.setStars(review.getStars());
        dto.setTags(review.getTags());
        dto.setComment(review.getComment());
        dto.setCustomerName(review.getCustomer().getName());
        dto.setProviderName(review.getProvider().getName());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
