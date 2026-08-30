package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.BookingStatus;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSessionStatus;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSessionRepository;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSessionServiceTest {
    private static final UUID BOOKING_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SESSION_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID COMMUNITY_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID SPOT_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID RESIDENT_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
    private static final UUID VEHICLE_ID = UUID.fromString("60000000-0000-0000-0000-000000000006");

    private static final Instant BOOKING_START = Instant.parse("2026-08-30T10:00:00Z");
    private static final Instant BOOKING_END = Instant.parse("2026-08-30T12:00:00Z");
    private static final Instant SESSION_START = Instant.parse("2026-08-30T10:05:00Z");
    private static final Instant SESSION_FINISH = Instant.parse("2026-08-30T11:00:00Z");

    private static final TimeRange TIME_RANGE = new TimeRange(BOOKING_START, BOOKING_END);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @InjectMocks
    private ParkingSessionService parkingSessionService;

    @Test
    void startsAndSavesParkingSession () {
        Booking booking = newBooking();
        ParkingSpot parkingSpot = newParkingSpot();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(parkingSessionRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.of(parkingSpot));
        when(parkingSessionRepository.existsActiveBySpotId(SPOT_ID)).thenReturn(false);

        ParkingSession result = parkingSessionService.startSession(BOOKING_ID, SESSION_START);

        ArgumentCaptor<ParkingSession> captor = ArgumentCaptor.forClass(ParkingSession.class);

        verify(bookingRepository).save(booking);
        verify(parkingSessionRepository).save(captor.capture());

        ParkingSession savedSession = captor.getValue();

        assertAll(
                () -> assertSame(savedSession, result),
                () -> assertNotNull(savedSession.getId()),
                () -> assertEquals(BOOKING_ID, savedSession.getBookingId()),
                () -> assertEquals(SPOT_ID, savedSession.getSpotId()),
                () -> assertEquals(VEHICLE_ID, savedSession.getVehicleId()),
                () -> assertEquals(SESSION_START, savedSession.getStartedAt()),
                () -> assertNull(savedSession.getFinishedAt()),
                () -> assertEquals(ParkingSessionStatus.ACTIVE, savedSession.getStatus()),
                () -> assertEquals(BookingStatus.USED, booking.getStatus())
        );
    }

    @Test
    void rejectsMissingBooking () {
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> parkingSessionService.startSession(BOOKING_ID, SESSION_START)
        );

        verifyNoInteractions(parkingSessionRepository, parkingSpotRepository);
    }

    @Test
    void rejectsExistingSessionForBooking () {
        Booking booking = newBooking();

        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking));
        when(parkingSessionRepository.existsByBookingId(BOOKING_ID))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> parkingSessionService.startSession(
                        BOOKING_ID,
                        SESSION_START
                )
        );

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        verifyNoInteractions(parkingSpotRepository);
        verify(bookingRepository, never()).save(any());
        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void rejectsMissingParkingSpot () {
        Booking booking = newBooking();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(parkingSessionRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> parkingSessionService.startSession(BOOKING_ID, SESSION_START)
        );

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        verify(bookingRepository, never()).save(any());
        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void rejectsInoperativeParkingSpot () {
        Booking booking = newBooking();
        ParkingSpot parkingSpot = newParkingSpot();
        parkingSpot.markInoperative();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(parkingSessionRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.of(parkingSpot));

        assertThrows(
                IllegalStateException.class,
                () -> parkingSessionService.startSession(BOOKING_ID, SESSION_START)
        );

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        verify(parkingSessionRepository, never()).existsActiveBySpotId(any());

        verify(bookingRepository, never()).save(any());
        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void rejectsParkingSpotWithActiveSession () {
        Booking booking = newBooking();
        ParkingSpot parkingSpot = newParkingSpot();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(parkingSessionRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.of(parkingSpot));
        when(parkingSessionRepository.existsActiveBySpotId(SPOT_ID)).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> parkingSessionService.startSession(BOOKING_ID, SESSION_START)
        );

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        verify(bookingRepository, never()).save(any());
        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void rejectsStartOutsideBookingArrivalWindow () {
        Booking booking = newBooking();
        ParkingSpot parkingSpot = newParkingSpot();
        Instant tooEarly = BOOKING_START.minusSeconds(1);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(parkingSessionRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.of(parkingSpot));
        when(parkingSessionRepository.existsActiveBySpotId(SPOT_ID)).thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> parkingSessionService.startSession(BOOKING_ID, tooEarly)
        );

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        verify(bookingRepository, never()).save(any());
        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void finishesAndSavesParkingSession () {
        ParkingSession parkingSession = newParkingSession();

        when(parkingSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(parkingSession));

        ParkingSession result = parkingSessionService.finishSession(SESSION_ID, SESSION_FINISH);

        assertAll(
                () -> assertSame(parkingSession, result),
                () -> assertEquals(ParkingSessionStatus.FINISHED, result.getStatus()),
                () -> assertEquals(SESSION_FINISH, result.getFinishedAt())
        );

        verify(parkingSessionRepository).save(parkingSession);
        verifyNoInteractions(bookingRepository, parkingSpotRepository);
    }

    @Test
    void rejectsMissingParkingSessionWhenFinishing () {
        when(parkingSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> parkingSessionService.finishSession(SESSION_ID, SESSION_FINISH)
        );

        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void rejectsFinishingAlreadyFinishedSession () {
        ParkingSession parkingSession = newParkingSession();
        parkingSession.finish(SESSION_FINISH);

        when(parkingSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(parkingSession));

        assertThrows(
                IllegalStateException.class,
                () -> parkingSessionService.finishSession(SESSION_ID, SESSION_FINISH.plusSeconds(60)
                )
        );

        verify(parkingSessionRepository, never()).save(any());
    }

    @Test
    void rejectsNullInputsBeforeUsingRepositories () {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> parkingSessionService.startSession(null, SESSION_START)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> parkingSessionService.startSession(BOOKING_ID, null)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> parkingSessionService.finishSession(null, SESSION_FINISH)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> parkingSessionService.finishSession(SESSION_ID, null)
                )
        );

        verifyNoInteractions(bookingRepository, parkingSessionRepository, parkingSpotRepository);
    }

    private Booking newBooking () {
        return new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);
    }

    private ParkingSpot newParkingSpot () {
        return new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-12");
    }

    private ParkingSession newParkingSession () {
        return new ParkingSession(SESSION_ID, BOOKING_ID, SPOT_ID, VEHICLE_ID, SESSION_START);
    }
}
