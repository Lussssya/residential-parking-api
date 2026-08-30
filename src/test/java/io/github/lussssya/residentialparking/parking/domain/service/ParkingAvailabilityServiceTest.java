package io.github.lussssya.residentialparking.parking.domain.service;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingAvailabilityServiceTest {
    private static final UUID SPOT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COMMUNITY_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final TimeRange REQUESTED_RANGE = new TimeRange(
            Instant.parse("2026-08-29T14:00:00Z"),
            Instant.parse("2026-08-29T16:00:00Z")
    );

    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private ParkingAvailabilityService availabilityService;

    @Test
    void reportsActiveSpotAvailableWhenNoBookingOverlaps () {
        ParkingSpot spot = newParkingSpot();

        when(bookingRepository.existsOverlappingBooking(
                SPOT_ID,
                REQUESTED_RANGE
        )).thenReturn(false);

        boolean available = availabilityService.isAvailable(spot, REQUESTED_RANGE);

        assertTrue(available);
        verify(bookingRepository).existsOverlappingBooking(SPOT_ID, REQUESTED_RANGE);
    }

    @Test
    void reportsActiveSpotUnavailableWhenBookingOverlaps () {
        ParkingSpot spot = newParkingSpot();

        when(bookingRepository.existsOverlappingBooking(
                SPOT_ID,
                REQUESTED_RANGE
        )).thenReturn(true);

        boolean available = availabilityService.isAvailable(spot, REQUESTED_RANGE);

        assertFalse(available);
        verify(bookingRepository).existsOverlappingBooking(SPOT_ID, REQUESTED_RANGE);
    }

    @Test
    void reportsInoperativeSpotUnavailableWithoutCheckingBookings () {
        ParkingSpot spot = newParkingSpot();
        spot.markInoperative();

        boolean available = availabilityService.isAvailable(spot, REQUESTED_RANGE);

        assertFalse(available);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void rejectsNullParkingSpot () {
        assertThrows(
                NullPointerException.class,
                () -> availabilityService.isAvailable(null, REQUESTED_RANGE)
        );

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void rejectsNullRequestedRange () {
        ParkingSpot spot = newParkingSpot();

        assertThrows(
                NullPointerException.class,
                () -> availabilityService.isAvailable(spot, null)
        );

        verifyNoInteractions(bookingRepository);
    }

    private ParkingSpot newParkingSpot () {
        return new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-12");
    }
}
