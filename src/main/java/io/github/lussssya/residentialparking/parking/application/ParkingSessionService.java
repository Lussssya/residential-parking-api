package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpotStatus;
import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSessionRepository;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParkingSessionService {
    private final BookingRepository bookingRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    @Transactional
    public ParkingSession startSession (UUID bookingId, Instant startedAt) {
        Objects.requireNonNull(bookingId, "Booking Id should not be null.");
        Objects.requireNonNull(startedAt, "Start time should not be null.");

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NoSuchElementException("No booking with such Id.")
        );

        final UUID parkingSpotId = booking.getSpotId();
        if (parkingSessionRepository.existsByBookingId(bookingId)) {
            throw new IllegalStateException("Can not start session over another active session");
        }
        ParkingSpot parkingSpot = parkingSpotRepository.findById(parkingSpotId).orElseThrow(
                () -> new NoSuchElementException("No parking spot with such Id.")
        );
        if (parkingSpot.getStatus() != ParkingSpotStatus.ACTIVE) {
            throw new IllegalStateException("Parking spot is not active.");
        }

        if (parkingSessionRepository.existsActiveBySpotId(parkingSpotId)) {
            throw new IllegalStateException("Parking spot is currently occupied.");
        }

        booking.markUsed(startedAt);
        bookingRepository.save(booking);

        ParkingSession parkingSession = new ParkingSession(
                UUID.randomUUID(),
                bookingId,
                parkingSpotId,
                booking.getVehicleId(),
                startedAt
        );
        parkingSessionRepository.save(parkingSession);

        return parkingSession;
    }

    @Transactional
    public ParkingSession finishSession (UUID sessionId, Instant finishedAt) {
        Objects.requireNonNull(sessionId, "Session Id should not be null.");
        Objects.requireNonNull(finishedAt, "Finish time should not be null.");

        ParkingSession parkingSession = parkingSessionRepository.findById(sessionId).orElseThrow(
                () -> new NoSuchElementException("No parking session with such Id.")
        );
        parkingSession.finish(finishedAt);
        parkingSessionRepository.save(parkingSession);
        return parkingSession;
    }
}
