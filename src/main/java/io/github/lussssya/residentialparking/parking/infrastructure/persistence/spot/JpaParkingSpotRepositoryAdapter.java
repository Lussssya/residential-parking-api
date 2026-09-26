package io.github.lussssya.residentialparking.parking.infrastructure.persistence.spot;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaParkingSpotRepositoryAdapter implements ParkingSpotRepository {
    private final SpringDataParkingSpotRepository parkingSpotRepository;

    @Override
    public Optional<ParkingSpot> findById (UUID id) {
        return parkingSpotRepository.findById(id).map(ParkingSpotJpaEntity::toDomain);
    }

    @Override
    public List<ParkingSpot> findAvailableByCommunityId (UUID communityId, TimeRange timeRange) {
        return parkingSpotRepository.findAvailableByCommunityId(communityId, timeRange.start(), timeRange.end())
                .stream()
                .map(ParkingSpotJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void save (ParkingSpot parkingSpot) {
        ParkingSpotJpaEntity entity = ParkingSpotJpaEntity.fromDomain(parkingSpot);

        parkingSpotRepository.save(entity);
    }
}
