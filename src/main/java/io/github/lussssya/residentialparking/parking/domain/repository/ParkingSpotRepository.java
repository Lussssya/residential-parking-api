package io.github.lussssya.residentialparking.parking.domain.repository;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingSpotRepository {
    Optional<ParkingSpot> findById (UUID id);

    List<ParkingSpot> findAvailableByCommunityId (UUID communityId, TimeRange timeRange);

    void save (ParkingSpot parkingSpot);
}
