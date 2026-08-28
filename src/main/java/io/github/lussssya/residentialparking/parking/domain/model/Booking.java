package io.github.lussssya.residentialparking.parking.domain.model;

import lombok.Getter;

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

    private BookingStatus status;

    public Booking (UUID id, UUID communityId, UUID spotId, UUID residentId, UUID vehicleId, TimeRange timeRange) {
        this.id = Objects.requireNonNull(id, "Booking ID should not be null.");
        this.communityId = Objects.requireNonNull(communityId, "Community ID should not be null.");
        this.spotId = Objects.requireNonNull(spotId, "Parking spot ID should not be null.");
        this.residentId = Objects.requireNonNull(residentId, "Resident ID should not be null.");
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID should not be null.");
        this.timeRange = Objects.requireNonNull(timeRange, "Time range should not be null.");
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel () {
        ensureConfirmed();
        status = BookingStatus.CANCELLED;
    }

    public void markUsed () {
        ensureConfirmed();
        status = BookingStatus.USED;
    }

    public void expire () {
        ensureConfirmed();
        status = BookingStatus.EXPIRED;
    }

    private void ensureConfirmed () {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only a confirmed booking can change status.");
        }
    }
}
