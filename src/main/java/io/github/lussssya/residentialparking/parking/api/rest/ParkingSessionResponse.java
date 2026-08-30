package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record ParkingSessionResponse(
        UUID id,
        UUID bookingId,
        UUID spotId,
        UUID vehicleId,
        Instant startedAt,
        Instant finishedAt,
        ParkingSessionStatus status
) {

    public static ParkingSessionResponse from (ParkingSession parkingSession) {
        return new ParkingSessionResponse(
                parkingSession.getId(),
                parkingSession.getBookingId(),
                parkingSession.getSpotId(),
                parkingSession.getVehicleId(),
                parkingSession.getStartedAt(),
                parkingSession.getFinishedAt(),
                parkingSession.getStatus()
        );
    }
}
