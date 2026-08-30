package io.github.lussssya.residentialparking.parking.domain.repository;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;

import java.util.Optional;
import java.util.UUID;

public interface ParkingSessionRepository {
    Optional<ParkingSession> findById (UUID id);

    boolean existsByBookingId (UUID bookingId);

    boolean existsActiveBySpotId (UUID spotId);

    void save (ParkingSession parkingSession);
}
