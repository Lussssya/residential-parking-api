package io.github.lussssya.residentialparking.parking.domain.repository;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingSpotRepository {
    Optional<ParkingSpot> findById (UUID id);

    List<ParkingSpot> findAllByCommunityId (UUID communityId);

    void save (ParkingSpot parkingSpot);
}
