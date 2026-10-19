package com.roadresq.repository;

import com.roadresq.model.RatingReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingReviewRepository extends JpaRepository<RatingReview, Long> {
    List<RatingReview> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    List<RatingReview> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<RatingReview> findByRequestId(Long requestId);
}
