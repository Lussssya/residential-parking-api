package io.github.lussssya.residentialparking.parking.infrastructure.persistence.booking;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.BookingStatus;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class BookingJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "community_id", nullable = false, updatable = false)
    private UUID communityId;
    @Column(name = "spot_id", nullable = false, updatable = false)
    private UUID spotId;
    @Column(name = "resident_id", nullable = false, updatable = false)
    private UUID residentId;
    @Column(name = "vehicle_id", nullable = false, updatable = false)
    private UUID vehicleId;
    @Column(name = "start_time", nullable = false, updatable = false)
    private Instant startTime;
    @Column(name = "end_time", nullable = false, updatable = false)
    private Instant endTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    protected BookingJpaEntity () {
        // Required by JPA
    }

    private BookingJpaEntity (UUID id, UUID communityId, UUID spotId, UUID residentId, UUID vehicleId, Instant startTime, Instant endTime, BookingStatus status) {
        this.id = id;
        this.communityId = communityId;
        this.spotId = spotId;
        this.residentId = residentId;
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public static BookingJpaEntity fromDomain (Booking booking) {
        Objects.requireNonNull(booking, "Booking should not be null.");

        return new BookingJpaEntity(
                booking.getId(),
                booking.getCommunityId(),
                booking.getSpotId(),
                booking.getResidentId(),
                booking.getVehicleId(),
                booking.getTimeRange().start(),
                booking.getTimeRange().end(),
                booking.getStatus()
        );
    }

    public Booking toDomain () {
        TimeRange timeRange = new TimeRange(startTime, endTime);
        return Booking.fromExistingState(id, communityId, spotId, residentId, vehicleId, timeRange, status);
    }
}
