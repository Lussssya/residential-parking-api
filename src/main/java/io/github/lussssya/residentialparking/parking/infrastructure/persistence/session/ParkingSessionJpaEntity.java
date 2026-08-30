package io.github.lussssya.residentialparking.parking.infrastructure.persistence.session;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSessionStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "parking_sessions")
public class ParkingSessionJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "booking_id", nullable = false, updatable = false)
    private UUID bookingId;
    @Column(name = "spot_id", nullable = false, updatable = false)
    private UUID spotId;
    @Column(name = "vehicle_id", nullable = false, updatable = false)
    private UUID vehicleId;
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;
    @Column(name = "finished_at")
    private Instant finishedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ParkingSessionStatus status;

    protected ParkingSessionJpaEntity () {
        // Required by JPA
    }

    private ParkingSessionJpaEntity (UUID id, UUID bookingId, UUID spotId, UUID vehicleId, Instant startedAt, Instant finishedAt, ParkingSessionStatus status) {
        this.id = id;
        this.bookingId = bookingId;
        this.spotId = spotId;
        this.vehicleId = vehicleId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
    }

    public static ParkingSessionJpaEntity fromDomain (ParkingSession booking) {
        Objects.requireNonNull(booking, "Parking session should not be null.");

        return new ParkingSessionJpaEntity(
                booking.getId(),
                booking.getBookingId(),
                booking.getSpotId(),
                booking.getVehicleId(),
                booking.getStartedAt(),
                booking.getFinishedAt(),
                booking.getStatus()
        );
    }

    public ParkingSession toDomain () {
        return ParkingSession.fromExistingState(id, bookingId, spotId, vehicleId, startedAt, finishedAt, status);
    }
}
