package io.github.lussssya.residentialparking.parking.infrastructure.persistence.booking;

import io.github.lussssya.residentialparking.parking.domain.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(booking) > 0 THEN true ELSE false END
            FROM BookingJpaEntity booking
            WHERE booking.spotId = :spotId
              AND booking.status IN :statuses
              AND booking.startTime < :requestedEnd
              AND :requestedStart < booking.endTime
            """)
    boolean existsOverlappingBooking (
            @Param("spotId") UUID spotId,
            @Param("requestedStart") Instant requestedStart,
            @Param("requestedEnd") Instant requestedEnd,
            @Param("statuses") Collection<BookingStatus> statuses
    );
}
