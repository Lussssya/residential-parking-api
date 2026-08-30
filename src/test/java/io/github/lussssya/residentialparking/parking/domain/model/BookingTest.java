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
    private static final Instant START = Instant.parse("2026-08-29T10:00:00Z");
    private static final Instant END = Instant.parse("2026-08-29T12:00:00Z");
    private static final Instant CHECK_IN_DEADLINE = Instant.parse("2026-08-29T10:15:00Z");
    private static final Instant DURING_GRACE_PERIOD = Instant.parse("2026-08-29T10:05:00Z");
    private static final TimeRange TIME_RANGE = new TimeRange(START, END);

    @Test
    void createsConfirmedBookingWithCheckInDeadline () {
        Booking booking = newBooking();

        assertAll(
                () -> assertEquals(BOOKING_ID, booking.getId()),
                () -> assertEquals(COMMUNITY_ID, booking.getCommunityId()),
                () -> assertEquals(SPOT_ID, booking.getSpotId()),
                () -> assertEquals(RESIDENT_ID, booking.getResidentId()),
                () -> assertEquals(VEHICLE_ID, booking.getVehicleId()),
                () -> assertEquals(TIME_RANGE, booking.getTimeRange()),
                () -> assertEquals(
                        CHECK_IN_DEADLINE,
                        booking.getCheckInDeadline()
                ),
                () -> assertEquals(
                        BookingStatus.CONFIRMED,
                        booking.getStatus()
                )
        );
    }

    @Test
    void capsCheckInDeadlineAtBookingEnd () {
        Instant shortBookingEnd = START.plusSeconds(10 * 60);
        TimeRange shortRange = new TimeRange(START, shortBookingEnd);

        Booking booking = new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, shortRange);

        assertEquals(shortBookingEnd, booking.getCheckInDeadline());
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
    void cancelsBookingBeforeStart () {
        Booking booking = newBooking();
        booking.cancel(START.minusSeconds(1));

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void cancelsBookingDuringGracePeriod () {
        Booking booking = newBooking();
        booking.cancel(DURING_GRACE_PERIOD);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void rejectsCancellationAtCheckInDeadline () {
        Booking booking = newBooking();

        assertThrows(IllegalStateException.class, () -> booking.cancel(CHECK_IN_DEADLINE));
        assertConfirmed(booking);
    }

    @Test
    void marksBookingUsedAtStart () {
        Booking booking = newBooking();
        booking.markUsed(START);

        assertEquals(BookingStatus.USED, booking.getStatus());
    }

    @Test
    void marksBookingUsedDuringGracePeriod () {
        Booking booking = newBooking();
        booking.markUsed(DURING_GRACE_PERIOD);

        assertEquals(BookingStatus.USED, booking.getStatus());
    }

    @Test
    void rejectsUseBeforeBookingStart () {
        Booking booking = newBooking();

        assertThrows(IllegalStateException.class, () -> booking.markUsed(START.minusNanos(1)));
        assertConfirmed(booking);
    }

    @Test
    void rejectsUseAtCheckInDeadline () {
        Booking booking = newBooking();

        assertThrows(IllegalStateException.class, () -> booking.markUsed(CHECK_IN_DEADLINE));
        assertConfirmed(booking);
    }

    @Test
    void rejectsExpirationBeforeCheckInDeadline () {
        Booking booking = newBooking();

        assertThrows(IllegalStateException.class, () -> booking.expire(CHECK_IN_DEADLINE.minusNanos(1)));
        assertConfirmed(booking);
    }

    @Test
    void expiresBookingAtCheckInDeadline () {
        Booking booking = newBooking();
        booking.expire(CHECK_IN_DEADLINE);

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    @Test
    void expiresBookingAfterCheckInDeadline () {
        Booking booking = newBooking();
        booking.expire(CHECK_IN_DEADLINE.plusSeconds(1));

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    @Test
    void rejectsNullCurrentTimesWithoutChangingBookings () {
        Booking cancellation = newBooking();
        Booking usage = newBooking();
        Booking expiration = newBooking();

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> cancellation.cancel(null)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> usage.markUsed(null)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> expiration.expire(null)
                )
        );

        assertAll(
                () -> assertConfirmed(cancellation),
                () -> assertConfirmed(usage),
                () -> assertConfirmed(expiration)
        );
    }

    @Test
    void rejectsTransitionsAfterCancellation () {
        Booking booking = newBooking();
        booking.cancel(DURING_GRACE_PERIOD);

        assertAllTransitionsRejected(booking);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void rejectsTransitionsAfterBeingUsed () {
        Booking booking = newBooking();
        booking.markUsed(DURING_GRACE_PERIOD);

        assertAllTransitionsRejected(booking);
        assertEquals(BookingStatus.USED, booking.getStatus());
    }

    @Test
    void rejectsTransitionsAfterExpiration () {
        Booking booking = newBooking();
        booking.expire(CHECK_IN_DEADLINE);

        assertAllTransitionsRejected(booking);
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    private Booking newBooking () {
        return new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);
    }

    private void assertConfirmed (Booking booking) {
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    private void assertAllTransitionsRejected (Booking booking) {
        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.cancel(DURING_GRACE_PERIOD)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.markUsed(DURING_GRACE_PERIOD)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.expire(CHECK_IN_DEADLINE)
                )
        );
    }
}
