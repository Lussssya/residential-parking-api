package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.BookingStatus;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import io.github.lussssya.residentialparking.parking.domain.service.ParkingAvailabilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private static final UUID BOOKING_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
    private static final UUID COMMUNITY_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_COMMUNITY_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID SPOT_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID RESIDENT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID VEHICLE_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final TimeRange TIME_RANGE = new TimeRange(
            Instant.parse("2026-08-30T10:00:00Z"),
            Instant.parse("2026-08-30T12:00:00Z")
    );

    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ParkingAvailabilityService parkingAvailabilityService;
    @InjectMocks
    private BookingService bookingService;

    @Test
    void createsAndSavesAvailableBooking () {
        ParkingSpot parkingSpot = newParkingSpot(COMMUNITY_ID);

        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.of(parkingSpot));
        when(parkingAvailabilityService.isAvailable(parkingSpot, TIME_RANGE)).thenReturn(true);

        Booking result = bookingService.createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking savedBooking = captor.getValue();

        assertAll(
                () -> assertSame(savedBooking, result),
                () -> assertNotNull(savedBooking.getId()),
                () -> assertEquals(
                        COMMUNITY_ID,
                        savedBooking.getCommunityId()
                ),
                () -> assertEquals(SPOT_ID, savedBooking.getSpotId()),
                () -> assertEquals(
                        RESIDENT_ID,
                        savedBooking.getResidentId()
                ),
                () -> assertEquals(
                        VEHICLE_ID,
                        savedBooking.getVehicleId()
                ),
                () -> assertEquals(
                        TIME_RANGE,
                        savedBooking.getTimeRange()
                ),
                () -> assertEquals(
                        BookingStatus.CONFIRMED,
                        savedBooking.getStatus()
                )
        );

        verify(parkingAvailabilityService).isAvailable(parkingSpot, TIME_RANGE);
    }

    @Test
    void rejectsMissingParkingSpot () {
        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> bookingService.createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
        );

        verifyNoInteractions(parkingAvailabilityService, bookingRepository);
    }

    @Test
    void rejectsParkingSpotFromAnotherCommunity () {
        ParkingSpot parkingSpot = newParkingSpot(OTHER_COMMUNITY_ID);

        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.of(parkingSpot));

        assertThrows(
                NoSuchElementException.class,
                () -> bookingService.createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
        );

        verifyNoInteractions(parkingAvailabilityService, bookingRepository);
    }

    @Test
    void rejectsUnavailableParkingSpot () {
        ParkingSpot parkingSpot = newParkingSpot(COMMUNITY_ID);

        when(parkingSpotRepository.findById(SPOT_ID)).thenReturn(Optional.of(parkingSpot));
        when(parkingAvailabilityService.isAvailable(parkingSpot, TIME_RANGE)).thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
        );

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void rejectsNullInputsBeforeUsingRepositories () {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.createBooking(null, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.createBooking(COMMUNITY_ID, null, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.createBooking(COMMUNITY_ID, SPOT_ID, null, VEHICLE_ID, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, null, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, null)
                )
        );

        verifyNoInteractions(parkingSpotRepository, parkingAvailabilityService, bookingRepository);
    }

    @Test
    void separatesCurrentAndFutureBookings () {
        Instant now = Instant.parse("2026-08-30T11:00:00Z");

        Booking currentBooking = new Booking(
                UUID.fromString("50000000-0000-0000-0000-000000000005"),
                COMMUNITY_ID,
                SPOT_ID,
                RESIDENT_ID,
                VEHICLE_ID,
                new TimeRange(Instant.parse("2026-08-30T10:00:00Z"), Instant.parse("2026-08-30T12:00:00Z"))
        );

        Booking futureBooking = new Booking(
                UUID.fromString("60000000-0000-0000-0000-000000000006"),
                COMMUNITY_ID,
                SPOT_ID,
                RESIDENT_ID,
                VEHICLE_ID,
                new TimeRange(Instant.parse("2026-08-30T13:00:00Z"), Instant.parse("2026-08-30T14:00:00Z"))
        );

        when(bookingRepository.findCurrentAndFutureByResidentId(RESIDENT_ID, now))
                .thenReturn(List.of(currentBooking, futureBooking));

        ResidentBookings result = bookingService.findCurrentAndFutureBookings(RESIDENT_ID, now);

        assertAll(
                () -> assertEquals(List.of(currentBooking), result.current()),
                () -> assertEquals(List.of(futureBooking), result.future())
        );

        verify(bookingRepository).findCurrentAndFutureByResidentId(RESIDENT_ID, now);

        verifyNoInteractions(parkingSpotRepository, parkingAvailabilityService);
    }

    @Test
    void rejectsNullInputsWhenFindingResidentBookings () {
        Instant now = Instant.parse("2026-08-30T11:00:00Z");

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.findCurrentAndFutureBookings(null, now)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.findCurrentAndFutureBookings(RESIDENT_ID, null)
                )
        );

        verifyNoInteractions(bookingRepository, parkingSpotRepository, parkingAvailabilityService);
    }

    @Test
    void cancelsConfirmedBookingAndSavesIt () {
        Booking booking = newBooking();
        Instant now = Instant.parse("2026-08-30T10:00:00Z");

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        Booking result = bookingService.cancelBooking(BOOKING_ID, now);

        assertAll(
                () -> assertSame(booking, result),
                () -> assertEquals(BookingStatus.CANCELLED, result.getStatus())
        );
        verify(bookingRepository).save(booking);
    }

    @Test
    void rejectsUnknownBookingWithoutSaving () {
        Instant now = Instant.parse("2026-08-30T10:00:00Z");

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, now)
        );

        verify(bookingRepository).findById(BOOKING_ID);
        verifyNoMoreInteractions(bookingRepository);
        verifyNoInteractions(parkingSpotRepository, parkingAvailabilityService);
    }

    @Test
    void propagatesCancellationFailureWithoutSaving () {
        Booking booking = newBooking();
        Instant checkInDeadline = Instant.parse("2026-08-30T10:15:00Z");

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, checkInDeadline)
        );

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository).findById(BOOKING_ID);
        verifyNoMoreInteractions(bookingRepository);
        verifyNoInteractions(parkingSpotRepository, parkingAvailabilityService);
    }

    @Test
    void rejectsNullInputsWhenCancellingBookingBeforeUsingRepositories () {
        Instant now = Instant.parse("2026-08-30T10:00:00Z");

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.cancelBooking(null, now)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> bookingService.cancelBooking(BOOKING_ID, null)
                )
        );

        verifyNoInteractions(bookingRepository, parkingSpotRepository, parkingAvailabilityService);
    }

    private ParkingSpot newParkingSpot (UUID communityId) {
        return new ParkingSpot(SPOT_ID, communityId, "A-12");
    }

    private Booking newBooking () {
        return new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);
    }
}
