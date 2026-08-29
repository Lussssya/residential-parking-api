package io.github.lussssya.residentialparking.parking.domain.repository;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Optional<Booking> findById (UUID id);

    boolean existsOverlappingBooking (UUID spotId, TimeRange timeRange);

    void save (Booking booking);
}
