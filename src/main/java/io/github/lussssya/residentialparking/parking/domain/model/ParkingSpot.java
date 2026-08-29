package io.github.lussssya.residentialparking.parking.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class ParkingSpot {
    private final UUID id;
    private final UUID communityId;
    private final String code;

    private ParkingSpotStatus status;

    public ParkingSpot (UUID id, UUID communityId, String code) {
        this.id = Objects.requireNonNull(id, "Parking spot ID should not be null.");
        this.communityId = Objects.requireNonNull(communityId, "Community ID should not be null.");

        Objects.requireNonNull(code, "Parking spot code should not be null.");
        if (code.isBlank()) {
            throw new IllegalArgumentException("Parking spot code should must not be blank.");
        }
        this.code = code.strip();

        this.status = ParkingSpotStatus.ACTIVE;
    }

    public void activate () {
        if (status != ParkingSpotStatus.INOPERATIVE) {
            throw new IllegalStateException("Only an inoperative parking spot can become active.");
        }
        status = ParkingSpotStatus.ACTIVE;
    }

    public void markInoperative () {
        if (status != ParkingSpotStatus.ACTIVE) {
            throw new IllegalStateException("Only an active parking spot can become inoperative.");
        }
        status = ParkingSpotStatus.INOPERATIVE;
    }

}
