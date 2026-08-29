package io.github.lussssya.residentialparking.parking.infrastructure.persistence.booking;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.BookingStatus;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaBookingRepositoryAdapter implements BookingRepository {
    private final SpringDataBookingRepository bookingRepository;

    @Override
    public Optional<Booking> findById (UUID id) {
        return bookingRepository.findById(id).map(BookingJpaEntity::toDomain);
    }

    @Override
    public boolean existsOverlappingBooking (UUID spotId, TimeRange timeRange) {
        final EnumSet<BookingStatus> statuses = EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.USED);

        return bookingRepository.existsOverlappingBooking(spotId, timeRange.start(), timeRange.end(), statuses);
    }

    @Override
    public void save (Booking booking) {
        BookingJpaEntity bookingJpaEntity = BookingJpaEntity.fromDomain(booking);

        bookingRepository.save(bookingJpaEntity);
    }
}
