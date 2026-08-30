package io.github.lussssya.residentialparking.parking.infrastructure.persistence.session;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataParkingSessionRepository extends JpaRepository<ParkingSessionJpaEntity, UUID> {

    boolean existsByBookingId (UUID bookingId);

    boolean existsBySpotIdAndStatus (UUID spotId, ParkingSessionStatus status);
}