package com.roadresq.service;

import com.roadresq.model.ProviderProfile;
import com.roadresq.model.ServiceType;
import com.roadresq.model.User;
import com.roadresq.repository.ProviderProfileRepository;
import com.roadresq.util.GeoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final ProviderProfileRepository providerProfileRepository;

    public ProviderService(ProviderProfileRepository providerProfileRepository) {
        this.providerProfileRepository = providerProfileRepository;
    }

    public List<Map<String, Object>> getAvailableProviders(Double userLat, Double userLon) {
        List<ProviderProfile> online = providerProfileRepository.findByIsOnlineTrue();

        return online.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("userId", p.getUser().getId());
            map.put("businessName", p.getBusinessName());
            map.put("rating", p.getRating());
            map.put("totalReviews", p.getTotalReviews());
            map.put("serviceTypes", p.getServiceTypes());
            map.put("latitude", p.getLatitude());
            map.put("longitude", p.getLongitude());
            map.put("activeVehiclePlate", p.getActiveVehiclePlate());
            map.put("isOnline", p.getIsOnline());

            if (userLat != null && userLon != null) {
                double distance = GeoUtils.calculateDistanceKm(userLat, userLon, p.getLatitude(), p.getLongitude());
                map.put("distanceKm", distance);
                map.put("etaMinutes", GeoUtils.calculateEtaMinutes(distance));
            }

            return map;
        }).sorted((a, b) -> {
            if (a.containsKey("distanceKm") && b.containsKey("distanceKm")) {
                return Double.compare((Double) a.get("distanceKm"), (Double) b.get("distanceKm"));
            }
            return 0;
        }).collect(Collectors.toList());
    }

    /**
     * Intelligent matching: filters by service capability and sorts by distance & rating
     */
    public List<Map<String, Object>> matchProviders(ServiceType serviceType, double customerLat, double customerLon) {
        List<ProviderProfile> online = providerProfileRepository.findByIsOnlineTrue();
        String serviceKey = serviceType != null ? serviceType.name() : "";

        return online.stream()
                .filter(p -> p.getServiceTypes().contains(serviceKey) || p.getServiceTypes().contains("ALL") || serviceKey.isEmpty())
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("userId", p.getUser().getId());
                    map.put("businessName", p.getBusinessName());
                    map.put("rating", p.getRating());
                    map.put("serviceTypes", p.getServiceTypes());
                    map.put("latitude", p.getLatitude());
                    map.put("longitude", p.getLongitude());
                    double distance = GeoUtils.calculateDistanceKm(customerLat, customerLon, p.getLatitude(), p.getLongitude());
                    map.put("distanceKm", distance);
                    map.put("etaMinutes", GeoUtils.calculateEtaMinutes(distance));
                    return map;
                })
                .sorted(Comparator.comparingDouble(m -> (Double) m.get("distanceKm")))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProviderProfile toggleOnlineStatus(User user, boolean isOnline) {
        ProviderProfile profile = providerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Provider profile not found"));
        profile.setIsOnline(isOnline);
        return providerProfileRepository.save(profile);
    }

    @Transactional
    public ProviderProfile updateLocation(User user, double latitude, double longitude) {
        ProviderProfile profile = providerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Provider profile not found"));
        profile.setLatitude(latitude);
        profile.setLongitude(longitude);
        return providerProfileRepository.save(profile);
    }

    public Optional<ProviderProfile> getProfileByUserId(Long userId) {
        return providerProfileRepository.findByUserId(userId);
    }
}
