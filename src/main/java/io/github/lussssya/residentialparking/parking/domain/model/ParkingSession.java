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
        this(id, bookingId, spotId, vehicleId, startedAt, null, ParkingSessionStatus.ACTIVE);
    }

    private ParkingSession (UUID id, UUID bookingId, UUID spotId, UUID vehicleId, Instant startedAt, Instant finishedAt, ParkingSessionStatus status) {
        this.id = Objects.requireNonNull(id, "Parking session id should not be null.");
        this.bookingId = Objects.requireNonNull(bookingId, "Booking id should not be null.");
        this.spotId = Objects.requireNonNull(spotId, "Parking spot id should not be null.");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle id should not be null.");
        this.startedAt = Objects.requireNonNull(startedAt, "Start time should not be null.");
        this.status = Objects.requireNonNull(status, "Status should not be null.");

        validateExistingState(finishedAt, status);
        this.finishedAt = finishedAt;
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

    private void validateExistingState (Instant finishedAt, ParkingSessionStatus status) {
        if (status == ParkingSessionStatus.ACTIVE && finishedAt != null) {
            throw new IllegalArgumentException("An active parking session cannot have a finish time.");
        }

        if (status == ParkingSessionStatus.FINISHED) {
            Objects.requireNonNull(finishedAt, "A finished parking session must have a finish time.");

            if (!startedAt.isBefore(finishedAt)) {
                throw new IllegalArgumentException("Finish time should be after start time.");
            }
        }
    }

    public static ParkingSession fromExistingState (UUID id, UUID bookingId, UUID spotId, UUID vehicleId, Instant startedAt, Instant finishedAt, ParkingSessionStatus status) {
        return new ParkingSession(id, bookingId, spotId, vehicleId, startedAt, finishedAt, status);
    }
}
