package io.github.lussssya.residentialparking.parking.infrastructure.persistence.spot;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpotStatus;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "parking_spots")
public class ParkingSpotJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "community_id", nullable = false, updatable = false)
    private UUID communityId;
    @Column(name = "code", nullable = false, length = 50)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ParkingSpotStatus status;

    protected ParkingSpotJpaEntity () {
        // Required by JPA.
    }

    private ParkingSpotJpaEntity (UUID id, UUID communityId, String code, ParkingSpotStatus status) {
        this.id = id;
        this.communityId = communityId;
        this.code = code;
        this.status = status;
    }

    public static ParkingSpotJpaEntity fromDomain (ParkingSpot parkingSpot) {
        Objects.requireNonNull(parkingSpot, "Parking spot should not be null.");

        return new ParkingSpotJpaEntity(
                parkingSpot.getId(),
                parkingSpot.getCommunityId(),
                parkingSpot.getCode(),
                parkingSpot.getStatus()
        );
    }

    public ParkingSpot toDomain () {
        return ParkingSpot.fromExistingState(id, communityId, code, status);
    }
}
