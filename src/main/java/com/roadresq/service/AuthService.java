package com.roadresq.service;

import com.roadresq.dto.*;
import com.roadresq.model.*;
import com.roadresq.repository.ProviderProfileRepository;
import com.roadresq.repository.UserRepository;
import com.roadresq.repository.VehicleRepository;
import com.roadresq.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       ProviderProfileRepository providerProfileRepository,
                       VehicleRepository vehicleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User(
                request.getName().trim(),
                request.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone().trim(),
                request.getRole() != null ? request.getRole() : Role.CUSTOMER
        );

        user = userRepository.save(user);

        ProviderProfile profile = null;
        if (user.getRole() == Role.PROVIDER) {
            String bName = request.getBusinessName() != null ? request.getBusinessName() : (user.getName() + " Road Services");
            String services = request.getServiceTypes() != null ? request.getServiceTypes() : "TOWING,FLAT_TYRE,BATTERY_JUMP,FUEL_DELIVERY";
            Double lat = request.getLatitude() != null ? request.getLatitude() : 37.7749;
            Double lon = request.getLongitude() != null ? request.getLongitude() : -122.4194;
            String plate = request.getActiveVehiclePlate() != null ? request.getActiveVehiclePlate() : "RESQ-01";

            profile = new ProviderProfile(user, bName, services, lat, lon, plate);
            profile = providerProfileRepository.save(profile);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(), user.getName());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), profile, "Registration successful");
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        ProviderProfile profile = null;
        if (user.getRole() == Role.PROVIDER) {
            profile = providerProfileRepository.findByUserId(user.getId()).orElse(null);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(), user.getName());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), profile, "Login successful");
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User updateProfile(User user, ProfileUpdateRequest request) {
        user.setName(request.getName().trim());
        user.setPhone(request.getPhone().trim());
        return userRepository.save(user);
    }

    public List<Vehicle> getUserVehicles(Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    @Transactional
    public Vehicle addVehicle(User user, VehicleDto dto) {
        Vehicle vehicle = new Vehicle(
                user,
                dto.getMake().trim(),
                dto.getModel().trim(),
                dto.getYear(),
                dto.getLicensePlate().trim().toUpperCase(),
                dto.getVehicleType(),
                dto.getFuelType(),
                dto.getColor()
        );
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(User user, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized to delete this vehicle");
        }
        vehicleRepository.delete(vehicle);
    }
}
