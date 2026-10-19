package com.roadresq.config;

import com.roadresq.model.*;
import com.roadresq.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final EmergencyContactRepository contactRepository;
    private final EmergencyRequestRepository requestRepository;
    private final RatingReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProviderProfileRepository providerProfileRepository,
                           VehicleRepository vehicleRepository,
                           EmergencyContactRepository contactRepository,
                           EmergencyRequestRepository requestRepository,
                           RatingReviewRepository reviewRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.vehicleRepository = vehicleRepository;
        this.contactRepository = contactRepository;
        this.requestRepository = requestRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already exists
        }

        // 1. Seed Customer (Aarav Sharma - Yamunanagar, Haryana)
        User customer = new User(
                "Aarav Sharma",
                "customer@roadresq.com",
                passwordEncoder.encode("password123"),
                "+91 98765 43210",
                Role.CUSTOMER
        );
        customer = userRepository.save(customer);

        // Seed Customer Indian Vehicles
        Vehicle vehicle1 = new Vehicle(customer, "Tata", "Nexon EV", 2023, "HR-02-AB-4567", "SUV", "Electric", "Daytona Grey");
        Vehicle vehicle2 = new Vehicle(customer, "Maruti Suzuki", "Swift", 2022, "HR-02-XY-8899", "Hatchback", "Petrol", "Solid Fire Red");
        Vehicle vehicle3 = new Vehicle(customer, "Mahindra", "Scorpio-N", 2024, "HR-03-MN-1122", "SUV", "Diesel", "Everest White");
        vehicleRepository.save(vehicle1);
        vehicleRepository.save(vehicle2);
        vehicleRepository.save(vehicle3);

        // Seed Customer Trusted Emergency Contacts (India)
        EmergencyContact contact1 = new EmergencyContact(customer, "Priya Sharma", "+91 98765 11223", "priya.sharma@example.in", "Spouse", true);
        EmergencyContact contact2 = new EmergencyContact(customer, "Rohit Sharma", "+91 98765 33445", "rohit.sharma@example.in", "Brother", true);
        contactRepository.save(contact1);
        contactRepository.save(contact2);

        // 2. Seed Certified Indian Rescue Providers
        // Provider 1: Haryana Roadside Assistance (Model Town, Yamunanagar)
        User providerUser1 = new User(
                "Rajesh Kumar",
                "provider1@roadresq.com",
                passwordEncoder.encode("password123"),
                "+91 98120 12345",
                Role.PROVIDER
        );
        providerUser1 = userRepository.save(providerUser1);
        ProviderProfile profile1 = new ProviderProfile(
                providerUser1,
                "Haryana Roadside Assistance",
                "TOWING,FLAT_TYRE,ENGINE_BREAKDOWN,OVERHEATING,ACCIDENT_ASSISTANCE",
                30.1380, 77.2750,
                "HR-02-T-9001"
        );
        profile1.setRating(4.96);
        profile1.setTotalReviews(48);
        providerProfileRepository.save(profile1);

        // Provider 2: Bharat Auto Rescue (Workshop Road, Jagadhri, Yamunanagar)
        User providerUser2 = new User(
                "Vikram Singh",
                "provider2@roadresq.com",
                passwordEncoder.encode("password123"),
                "+91 98120 54321",
                Role.PROVIDER
        );
        providerUser2 = userRepository.save(providerUser2);
        ProviderProfile profile2 = new ProviderProfile(
                providerUser2,
                "Bharat Auto Rescue",
                "BATTERY_JUMP,FLAT_TYRE,FUEL_DELIVERY,LOCKOUT",
                30.1450, 77.2990,
                "HR-02-M-4040"
        );
        profile2.setRating(4.89);
        profile2.setTotalReviews(62);
        providerProfileRepository.save(profile2);

        // Provider 3: QuickFix Road Services (Fountain Chowk, Yamunanagar)
        User providerUser3 = new User(
                "Amit Verma",
                "provider3@roadresq.com",
                passwordEncoder.encode("password123"),
                "+91 98120 99887",
                Role.PROVIDER
        );
        providerUser3 = userRepository.save(providerUser3);
        ProviderProfile profile3 = new ProviderProfile(
                providerUser3,
                "QuickFix Road Services",
                "EV_CHARGE,BATTERY_JUMP,FLAT_TYRE,ENGINE_BREAKDOWN",
                30.1210, 77.2550,
                "HR-01-EV-3300"
        );
        profile3.setRating(4.98);
        profile3.setTotalReviews(31);
        providerProfileRepository.save(profile3);

        // Provider 4: Shree Ganesh Recovery & Towing (NH-344 Bypass, Yamunanagar)
        User providerUser4 = new User(
                "Suresh Sharma",
                "provider4@roadresq.com",
                passwordEncoder.encode("password123"),
                "+91 98120 66778",
                Role.PROVIDER
        );
        providerUser4 = userRepository.save(providerUser4);
        ProviderProfile profile4 = new ProviderProfile(
                providerUser4,
                "Shree Ganesh Recovery & Towing",
                "TOWING,ACCIDENT_ASSISTANCE,FUEL_DELIVERY,OTHER",
                30.1600, 77.3100,
                "HR-04-TR-7711"
        );
        profile4.setRating(4.91);
        profile4.setTotalReviews(44);
        providerProfileRepository.save(profile4);

        // 3. Seed Completed Emergency Request in Yamunanagar
        EmergencyRequest pastReq = new EmergencyRequest();
        pastReq.setTrackingCode("RR-IN-9102");
        pastReq.setCustomer(customer);
        pastReq.setAssignedProvider(providerUser1);
        pastReq.setServiceType(ServiceType.BATTERY_JUMP);
        pastReq.setSeverity(Severity.NORMAL);
        pastReq.setStatus(RequestStatus.COMPLETED);
        pastReq.setLatitude(30.1345);
        pastReq.setLongitude(77.2710);
        pastReq.setAddress("Govindpuri Road, Near ITI Chowk, Yamunanagar, Haryana 135001");
        pastReq.setVehicleInfo("Tata Nexon EV (HR-02-AB-4567)");
        pastReq.setProblemDescription("Auxiliary 12V terminal voltage dropped; dash displayed electrical system alert.");
        pastReq.setBaseFee(new BigDecimal("499.00"));
        pastReq.setTravelFee(new BigDecimal("125.00"));
        pastReq.setHazardFee(BigDecimal.ZERO);
        pastReq.setTotalEstimatedCost(new BigDecimal("736.32"));
        pastReq.setQuoteStatus(QuoteStatus.ORIGINAL);
        pastReq.setConsentContacts(true);
        pastReq.setCreatedAt(LocalDateTime.now().minusDays(1));
        pastReq.setAssignedAt(LocalDateTime.now().minusDays(1).plusMinutes(2));
        pastReq.setCompletedAt(LocalDateTime.now().minusDays(1).plusMinutes(32));
        pastReq = requestRepository.save(pastReq);

        RatingReview review = new RatingReview(
                pastReq,
                customer,
                providerUser1,
                5,
                "Quick 12-minute arrival at Govindpuri Chowk, Highly Professional",
                "Rajesh ji from Haryana Roadside Assistance arrived in under 12 minutes! Tested auxiliary battery with digital multimeter and jumped vehicle smoothly without error codes. Outstanding service in Yamunanagar."
        );
        reviewRepository.save(review);

        // 4. Seed an Active Pending Emergency Request in Yamunanagar (Ready for Live Demo Acceptance)
        EmergencyRequest pendingReq = new EmergencyRequest();
        pendingReq.setTrackingCode("RR-IN-4081");
        pendingReq.setCustomer(customer);
        pendingReq.setServiceType(ServiceType.FLAT_TYRE);
        pendingReq.setSeverity(Severity.URGENT);
        pendingReq.setStatus(RequestStatus.PENDING);
        pendingReq.setLatitude(30.1390);
        pendingReq.setLongitude(77.2820);
        pendingReq.setAddress("Near Madhu Hotel, Jagadhri Road, Yamunanagar, Haryana 135003");
        pendingReq.setVehicleInfo("Maruti Suzuki Swift (HR-02-XY-8899)");
        pendingReq.setProblemDescription("Left rear tyre punctured by iron nail on Jagadhri Road. Vehicle parked safely on roadside shoulder.");
        pendingReq.setBaseFee(new BigDecimal("399.00"));
        pendingReq.setTravelFee(new BigDecimal("100.00"));
        pendingReq.setHazardFee(new BigDecimal("150.00"));
        pendingReq.setTotalEstimatedCost(new BigDecimal("765.82"));
        pendingReq.setQuoteStatus(QuoteStatus.ORIGINAL);
        pendingReq.setConsentContacts(true);
        pendingReq.setCreatedAt(LocalDateTime.now().minusMinutes(4));
        requestRepository.save(pendingReq);
    }
}
