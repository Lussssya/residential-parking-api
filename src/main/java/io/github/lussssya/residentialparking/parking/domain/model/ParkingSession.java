package io.github.lussssya.residentialparking.parking.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ParkingSession {
    private final UUID id;
    private final UUID bookingId;
    private final UUID spotId;
    private final UUID vehicleId;
    private final Instant startedAt;

    private Instant finishedAt;
    private ParkingSessionStatus status;

    public ParkingSession (UUID id, UUID bookingId, UUID spotId, UUID vehicleId, Instant startedAt) {
        this.id = Objects.requireNonNull(id, "Parking session id should not be null.");
        this.bookingId = Objects.requireNonNull(bookingId, "Booking id should not be null.");
        this.spotId = Objects.requireNonNull(spotId, "Parking spot id should not be null.");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle id should not be null.");
        this.startedAt = Objects.requireNonNull(startedAt, "Start time should not be null.");
        status = ParkingSessionStatus.ACTIVE;
    }

    public void finish (Instant finishedAt) {
        if (status != ParkingSessionStatus.ACTIVE) {
            throw new IllegalStateException("Only an active parking session can finish.");
        }

        Objects.requireNonNull(finishedAt, "Finish time should not be null.");
        if (!startedAt.isBefore(finishedAt)) {
            throw new IllegalArgumentException("Finish time should be after start time.");
        }

        this.finishedAt = finishedAt;
        this.status = ParkingSessionStatus.FINISHED;
    }
}
