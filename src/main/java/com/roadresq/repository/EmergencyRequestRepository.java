package com.roadresq.repository;

import com.roadresq.model.EmergencyRequest;
import com.roadresq.model.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {

    Optional<EmergencyRequest> findByTrackingCode(String trackingCode);

    List<EmergencyRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<EmergencyRequest> findByAssignedProviderIdOrderByCreatedAtDesc(Long providerId);

    List<EmergencyRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    /**
     * Pessimistic row-level lock for atomic First-Provider-Wins assignment.
     * Prevents race conditions and simultaneous acceptance conflicts.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM EmergencyRequest r WHERE r.id = :id")
    Optional<EmergencyRequest> findByIdForUpdate(@Param("id") Long id);
}
