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
    private Instant actualStartTime;
    private Instant actualFinishTime;

    private BookingStatus status;

    private static final Duration ARRIVAL_GRACE_PERIOD = Duration.ofMinutes(15);

    public Booking (UUID id, UUID communityId, UUID spotId, UUID residentId, UUID vehicleId, TimeRange timeRange) {
        this(id, communityId, spotId, residentId, vehicleId, timeRange, BookingStatus.CONFIRMED, null, null);
    }

    private Booking (
            UUID id,
            UUID communityId,
            UUID spotId,
            UUID residentId,
            UUID vehicleId,
            TimeRange timeRange,
            BookingStatus status,
            Instant actualStartTime,
            Instant actualFinishTime
    ) {
        this.id = Objects.requireNonNull(id, "Booking ID should not be null.");
        this.communityId = Objects.requireNonNull(communityId, "Community ID should not be null.");
        this.spotId = Objects.requireNonNull(spotId, "Parking spot ID should not be null.");
        this.residentId = Objects.requireNonNull(residentId, "Resident ID should not be null.");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID should not be null.");
        this.timeRange = Objects.requireNonNull(timeRange, "Time range should not be null.");
        this.status = Objects.requireNonNull(status, "Status should not be null.");
        validateLifecycleTimes(status, actualStartTime, actualFinishTime);
        this.actualStartTime = actualStartTime;
        this.actualFinishTime = actualFinishTime;
    }

    public static Booking create (UUID id, UUID communityId, UUID spotId, UUID residentId, UUID vehicleId, TimeRange timeRange, Instant now) {
        if (timeRange.start().isBefore(now)) {
            throw new IllegalStateException("Booking should not be in the past");
        }
        return new Booking(id, communityId, spotId, residentId, vehicleId, timeRange);
    }

    public void cancel (Instant now) {
        ensureConfirmed();
        Objects.requireNonNull(now, "Current time should not be null.");

        final Instant checkInDeadline = getCheckInDeadline();
        if (!now.isBefore(checkInDeadline)) {
            throw new IllegalStateException("A booking can only be cancelled before its check-in deadline.");
        }

        status = BookingStatus.CANCELLED;
    }

    public void activate (Instant now) {
        ensureConfirmed();
        Objects.requireNonNull(now, "Current time should not be null.");

        final Instant checkInDeadline = getCheckInDeadline();
        if (now.isBefore(timeRange.start()) || !now.isBefore(checkInDeadline)) {
            throw new IllegalStateException("A booking can only be used during its arrival window.");
        }

        actualStartTime = now;
        status = BookingStatus.ACTIVATED;
    }

    public void expire (Instant now) {
        ensureConfirmed();
        Objects.requireNonNull(now, "Current time should not be null.");

        final Instant checkInDeadline = getCheckInDeadline();
        if (now.isBefore(checkInDeadline)) {
            throw new IllegalStateException("A booking cannot expire before its check-in deadline.");
        }

        status = BookingStatus.EXPIRED;
    }

    public void complete (Instant now) {
        if (status != BookingStatus.ACTIVATED) {
            throw new IllegalStateException("Only an activated booking can be completed.");
        }
        Objects.requireNonNull(now, "Current time should not be null.");
        if (!actualStartTime.isBefore(now)) {
            throw new IllegalArgumentException("Completion time should be after activation time.");
        }

        actualFinishTime = now;
        status = BookingStatus.COMPLETED;
    }

    public Instant getCheckInDeadline () {
        Instant deadline = timeRange.start().plus(ARRIVAL_GRACE_PERIOD);
        return deadline.isBefore(timeRange.end()) ? deadline : timeRange.end();
    }

    private void ensureConfirmed () {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only a confirmed booking can change status.");
        }
    }

    private void validateLifecycleTimes (
            BookingStatus status,
            Instant actualStartTime,
            Instant actualFinishTime
    ) {
        if (status == BookingStatus.ACTIVATED) {
            Objects.requireNonNull(actualStartTime, "An activated booking must have an activation time.");
            if (actualFinishTime != null) {
                throw new IllegalArgumentException("An activated booking cannot have a completion time.");
            }
            return;
        }

        if (status == BookingStatus.COMPLETED) {
            Objects.requireNonNull(actualStartTime, "A completed booking must have an activation time.");
            Objects.requireNonNull(actualFinishTime, "A completed booking must have a completion time.");
            if (!actualStartTime.isBefore(actualFinishTime)) {
                throw new IllegalArgumentException("Completion time should be after activation time.");
            }
            return;
        }

        if (actualStartTime != null || actualFinishTime != null) {
            throw new IllegalArgumentException("An inactive booking cannot have actual lifecycle times.");
        }
    }

    public static Booking fromExistingState (
            UUID id,
            UUID communityId,
            UUID spotId,
            UUID residentId,
            UUID vehicleId,
            TimeRange timeRange,
            BookingStatus status,
            Instant actualStartTime,
            Instant actualFinishTime
    ) {
        return new Booking(id, communityId, spotId, residentId, vehicleId, timeRange, status, actualStartTime, actualFinishTime);
    }
}
