package io.github.lussssya.residentialparking.parking.infrastructure.persistence.spot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataParkingSpotRepository extends JpaRepository<ParkingSpotJpaEntity, UUID> {
    @Query(value = """
            SELECT ps.*
            FROM parking_spots ps
            WHERE ps.community_id = :communityId
              AND ps.status = 'ACTIVE'
              AND NOT EXISTS (
                  SELECT 1
                  FROM bookings b
                  WHERE b.spot_id = ps.id
                    AND b.status IN ('CONFIRMED', 'ACTIVATED')
                    AND b.start_time < :end
                    AND :start < b.end_time
              )
            ORDER BY ps.code
            """, nativeQuery = true)
    List<ParkingSpotJpaEntity> findAvailableByCommunityId (UUID communityId, Instant start, Instant end);
}
