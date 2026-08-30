package io.github.lussssya.residentialparking.parking.infrastructure.persistence.session;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSessionStatus;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaParkingSessionRepositoryAdapter implements ParkingSessionRepository {
    private final SpringDataParkingSessionRepository parkingSessionRepository;

    @Override
    public Optional<ParkingSession> findById (UUID id) {
        return parkingSessionRepository.findById(id).map(ParkingSessionJpaEntity::toDomain);
    }

    @Override
    public boolean existsByBookingId (UUID bookingId) {
        return parkingSessionRepository.existsByBookingId(bookingId);
    }

    @Override
    public boolean existsActiveBySpotId (UUID spotId) {
        return parkingSessionRepository.existsBySpotIdAndStatus(spotId, ParkingSessionStatus.ACTIVE);
    }

    @Override
    public void save (ParkingSession parkingSession) {
        ParkingSessionJpaEntity parkingSessionJpaEntity = ParkingSessionJpaEntity.fromDomain(parkingSession);

        parkingSessionRepository.save(parkingSessionJpaEntity);
    }
}
