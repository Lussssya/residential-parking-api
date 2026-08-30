package io.github.lussssya.residentialparking.parking.domain.repository;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Optional<Booking> findById (UUID id);

    boolean existsOverlappingBooking (UUID spotId, TimeRange timeRange);

    List<Booking> findCurrentAndFutureByResidentId (UUID residentId, Instant now);

    void save (Booking booking);
}
