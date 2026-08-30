package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import io.github.lussssya.residentialparking.parking.domain.service.ParkingAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final ParkingSpotRepository parkingSpotRepository;
    private final BookingRepository bookingRepository;
    private final ParkingAvailabilityService parkingAvailabilityService;

    @Transactional
    public Booking createBooking (UUID communityId, UUID spotId, UUID residentId, UUID vehicleId, TimeRange timeRange) {
        Objects.requireNonNull(communityId, "Community ID should not be null.");
        Objects.requireNonNull(spotId, "Parking spot ID should not be null.");
        Objects.requireNonNull(residentId, "Resident ID should not be null.");
        Objects.requireNonNull(vehicleId, "Vehicle ID should not be null.");
        Objects.requireNonNull(timeRange, "Time range should not be null.");

        final ParkingSpot parkingSpot = parkingSpotRepository.findById(spotId).orElseThrow(() -> new NoSuchElementException("Parking spot was not found."));

        if (!parkingSpot.getCommunityId().equals(communityId)) {
            throw new NoSuchElementException("Parking spot was not found in the community.");
        }

        if (!parkingAvailabilityService.isAvailable(parkingSpot, timeRange)) {
            throw new IllegalStateException("Parking spot is not available for the requested time.");
        }

        final Booking booking = new Booking(UUID.randomUUID(), communityId, spotId, residentId, vehicleId, timeRange);
        bookingRepository.save(booking);
        return booking;
    }

    @Transactional(readOnly = true)
    public ResidentBookings findCurrentAndFutureBookings (UUID residentId, Instant now) {
        Objects.requireNonNull(residentId, "Resident Id should not be null.");
        Objects.requireNonNull(now, "Current time should not be null.");

        List<Booking> bookings = bookingRepository.findCurrentAndFutureByResidentId(residentId, now);
        List<Booking> currentBookings = new ArrayList<>();
        List<Booking> futureBookings = new ArrayList<>();

        for (Booking booking : bookings) {
            if (!booking.getTimeRange().start().isAfter(now)) {
                currentBookings.add(booking);
            } else {
                futureBookings.add(booking);
            }
        }

        return new ResidentBookings(currentBookings, futureBookings);
    }
}
