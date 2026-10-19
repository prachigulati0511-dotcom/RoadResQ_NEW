package com.roadresq.util;

public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_DISPATCH_SPEED_KMH = 35.0; // Urban rescue average speed
    private static final int DISPATCH_PREP_MINUTES = 5;

    /**
     * Calculates distance between two coordinates in kilometers using Haversine formula
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        // Round to 1 decimal place
        return Math.round(distance * 10.0) / 10.0;
    }

    /**
     * Calculates estimated time of arrival (ETA) in minutes
     */
    public static int calculateEtaMinutes(double distanceKm) {
        if (distanceKm <= 0.1) {
            return 3;
        }
        double drivingHours = distanceKm / AVERAGE_DISPATCH_SPEED_KMH;
        int drivingMinutes = (int) Math.round(drivingHours * 60.0);
        return Math.max(5, drivingMinutes + DISPATCH_PREP_MINUTES);
    }
}
