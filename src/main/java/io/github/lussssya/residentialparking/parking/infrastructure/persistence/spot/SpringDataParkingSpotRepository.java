package io.github.lussssya.residentialparking.parking.infrastructure.persistence.spot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataParkingSpotRepository extends JpaRepository<ParkingSpotJpaEntity, UUID> {
}