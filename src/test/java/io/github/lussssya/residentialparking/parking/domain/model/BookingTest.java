package io.github.lussssya.residentialparking.parking.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingTest {
    private static final UUID BOOKING_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COMMUNITY_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID SPOT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID RESIDENT_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID VEHICLE_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
    private static final TimeRange TIME_RANGE = new TimeRange(
            Instant.parse("2026-08-28T10:00:00Z"),
            Instant.parse("2026-08-28T12:00:00Z")
    );

    @Test
    void createsConfirmedBooking () {
        Booking booking = newBooking();

        assertAll(
                () -> assertEquals(BOOKING_ID, booking.getId()),
                () -> assertEquals(COMMUNITY_ID, booking.getCommunityId()),
                () -> assertEquals(SPOT_ID, booking.getSpotId()),
                () -> assertEquals(RESIDENT_ID, booking.getResidentId()),
                () -> assertEquals(VEHICLE_ID, booking.getVehicleId()),
                () -> assertEquals(TIME_RANGE, booking.getTimeRange()),
                () -> assertEquals(
                        BookingStatus.CONFIRMED,
                        booking.getStatus()
                )
        );
    }

    @Test
    void rejectsNullRequiredValues () {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new Booking(null, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new Booking(BOOKING_ID, null, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new Booking(BOOKING_ID, COMMUNITY_ID, null, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, null, VEHICLE_ID, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, null, TIME_RANGE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, null)
                )
        );
    }

    @Test
    void cancelsConfirmedBooking () {
        Booking booking = newBooking();
        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void marksConfirmedBookingAsUsed () {
        Booking booking = newBooking();
        booking.markUsed();

        assertEquals(BookingStatus.USED, booking.getStatus());
    }

    @Test
    void expiresConfirmedBooking () {
        Booking booking = newBooking();
        booking.expire();

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    @Test
    void rejectsOperationsAfterCancellation () {
        Booking booking = newBooking();
        booking.cancel();

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::cancel
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::markUsed
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::expire
                )
        );

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void rejectsOperationsAfterBeingUsed () {
        Booking booking = newBooking();
        booking.markUsed();

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::cancel
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::markUsed
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::expire
                )
        );

        assertEquals(BookingStatus.USED, booking.getStatus());
    }

    @Test
    void rejectsOperationsAfterExpiration () {
        Booking booking = newBooking();
        booking.expire();

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::cancel
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::markUsed
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        booking::expire
                )
        );

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    private Booking newBooking () {
        return new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);
    }
}