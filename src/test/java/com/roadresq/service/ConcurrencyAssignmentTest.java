package com.roadresq.service;

import com.roadresq.dto.EmergencyRequestResponseDto;
import com.roadresq.model.*;
import com.roadresq.repository.EmergencyRequestRepository;
import com.roadresq.repository.ProviderProfileRepository;
import com.roadresq.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class ConcurrencyAssignmentTest {

    @Autowired
    private EmergencyRequestService emergencyRequestService;

    @Autowired
    private EmergencyRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderProfileRepository providerProfileRepository;

    private Long testRequestId;
    private List<User> testProviders;

    @BeforeEach
    void setUp() {
        // Create customer
        User customer = new User("Test Customer", "testcustomer@test.com", "hash", "+1000000000", Role.CUSTOMER);
        customer = userRepository.save(customer);

        // Create 5 test providers
        testProviders = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User p = new User("Provider " + i, "concurrency_p" + i + "_" + System.currentTimeMillis() + "@test.com", "hash", "+100000000" + i, Role.PROVIDER);
            p = userRepository.save(p);
            ProviderProfile prof = new ProviderProfile(p, "Rescue Unit " + i, "TOWING,FLAT_TYRE", 37.7749, -122.4194, "PLATE-" + i);
            providerProfileRepository.save(prof);
            testProviders.add(p);
        }

        // Create single pending emergency request
        EmergencyRequest req = new EmergencyRequest();
        req.setTrackingCode("RR-CONCUR-" + System.currentTimeMillis());
        req.setCustomer(customer);
        req.setServiceType(ServiceType.TOWING);
        req.setSeverity(Severity.URGENT);
        req.setStatus(RequestStatus.PENDING);
        req.setLatitude(37.7749);
        req.setLongitude(-122.4194);
        req.setAddress("Test Highway 101, Mile Marker 42");
        req.setBaseFee(new BigDecimal("85.00"));
        req.setTravelFee(new BigDecimal("10.00"));
        req.setHazardFee(new BigDecimal("15.00"));
        req.setTotalEstimatedCost(new BigDecimal("110.00"));
        req.setCreatedAt(LocalDateTime.now());
        req = requestRepository.save(req);
        testRequestId = req.getId();
    }

    @Test
    @DisplayName("Verify First-Provider-Wins: exactly 1 provider wins when 5 accept simultaneously")
    void testConcurrentAcceptanceAtomicLock() throws InterruptedException {
        int threadCount = testProviders.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final User provider = testProviders.get(i);
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Wait until all threads are armed and ready
                    EmergencyRequestResponseDto assigned = emergencyRequestService.acceptRequest(testRequestId, provider);
                    successCount.incrementAndGet();
                    results.add("SUCCESS: Provider " + provider.getId() + " won assignment");
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("Conflict") || e.getMessage().contains("assigned")) {
                        conflictCount.incrementAndGet();
                        results.add("CONFLICT: Provider " + provider.getId() + " was rejected (Already assigned)");
                    } else {
                        results.add("ERROR: " + e.getMessage());
                    }
                } catch (org.springframework.dao.ConcurrencyFailureException | org.springframework.transaction.TransactionException e) {
                    conflictCount.incrementAndGet();
                    results.add("CONFLICT (LOCK_TIMEOUT/DEADLOCK_PREVENTED): Provider " + provider.getId() + " rolled back");
                } catch (Exception e) {
                    if (e.getMessage() != null && (e.getMessage().contains("Deadlock") || e.getMessage().contains("Conflict") || e.getMessage().contains("assigned"))) {
                        conflictCount.incrementAndGet();
                        results.add("CONFLICT: Provider " + provider.getId() + " was rejected");
                    } else {
                        results.add("UNEXPECTED: " + e.getClass().getName() + " - " + e.getMessage());
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Fire simultaneously!
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Print results for validation
        results.forEach(System.out::println);

        // Strict assertions
        assertEquals(1, successCount.get(), "Exactly one provider must succeed");
        assertEquals(threadCount - 1, conflictCount.get(), "All other concurrent providers must receive conflict rejection");

        // Verify database state
        EmergencyRequest finalReq = requestRepository.findById(testRequestId).orElseThrow();
        assertEquals(RequestStatus.ACCEPTED, finalReq.getStatus());
        assertNotNull(finalReq.getAssignedProvider());
        assertNotNull(finalReq.getAssignedAt());
    }
}
