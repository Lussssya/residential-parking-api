package io.github.lussssya.residentialparking.parking.domain.service;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpotStatus;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class ParkingAvailabilityService {
    private final BookingRepository bookingRepository;

    public boolean isAvailable (ParkingSpot parkingSpot, TimeRange requestedRange) {
        Objects.requireNonNull(parkingSpot, "Parking spot should not be null.");
        Objects.requireNonNull(requestedRange, "Requested time range should not be null.");

        if (parkingSpot.getStatus() != ParkingSpotStatus.ACTIVE) {
            return false;
        }

        return !bookingRepository.existsOverlappingBooking(parkingSpot.getId(), requestedRange);
    }
}
