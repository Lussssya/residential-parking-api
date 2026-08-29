package io.github.lussssya.residentialparking.parking.domain.model;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Booking {
    private final UUID id;
    private final UUID communityId;
    private final UUID spotId;
    private final UUID residentId;
    private final UUID vehicleId;
    private final TimeRange timeRange;
    private final Instant checkInDeadline;

    private BookingStatus status;

    private static final Duration ARRIVAL_GRACE_PERIOD = Duration.ofMinutes(15);

    public Booking (UUID id, UUID communityId, UUID spotId, UUID residentId, UUID vehicleId, TimeRange timeRange) {
        this.id = Objects.requireNonNull(id, "Booking ID should not be null.");
        this.communityId = Objects.requireNonNull(communityId, "Community ID should not be null.");
        this.spotId = Objects.requireNonNull(spotId, "Parking spot ID should not be null.");
        this.residentId = Objects.requireNonNull(residentId, "Resident ID should not be null.");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID should not be null.");
        this.timeRange = Objects.requireNonNull(timeRange, "Time range should not be null.");

        Instant graceDeadline = timeRange.start().plus(ARRIVAL_GRACE_PERIOD);
        this.checkInDeadline = graceDeadline.isBefore(timeRange.end()) ? graceDeadline : timeRange.end();

        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel (Instant now) {
        ensureConfirmed();
        Objects.requireNonNull(now, "Current time should not be null.");

        if (!now.isBefore(checkInDeadline)) {
            throw new IllegalStateException("A booking can only be cancelled before its check-in deadline.");
        }

        status = BookingStatus.CANCELLED;
    }

    public void markUsed (Instant now) {
        ensureConfirmed();
        Objects.requireNonNull(now, "Current time should not be null.");

        if (now.isBefore(timeRange.start()) || !now.isBefore(checkInDeadline)) {
            throw new IllegalStateException("A booking can only be used during its arrival window.");
        }

        status = BookingStatus.USED;
    }

    public void expire (Instant now) {
        ensureConfirmed();
        Objects.requireNonNull(now, "Current time should not be null.");

        if (now.isBefore(checkInDeadline)) {
            throw new IllegalStateException("A booking cannot expire before its check-in deadline.");
        }

        status = BookingStatus.EXPIRED;
    }

    private void ensureConfirmed () {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only a confirmed booking can change status.");
        }
    }
}
