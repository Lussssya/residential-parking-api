package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParkingSpotService {
    private final ParkingSpotRepository parkingSpotRepository;

    @Transactional(readOnly = true)
    public List<ParkingSpot> getAvailableParkingSpots (UUID communityId, TimeRange timeRange) {
        Objects.requireNonNull(communityId, "Community ID should not be null.");
        Objects.requireNonNull(timeRange, "Time range should not be null.");

        return parkingSpotRepository.findAvailableByCommunityId(communityId, timeRange);
    }
}
