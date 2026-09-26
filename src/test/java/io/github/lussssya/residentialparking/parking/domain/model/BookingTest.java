package io.github.lussssya.residentialparking.parking.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    private static final Instant COMPLETION_TIME = Instant.parse("2026-08-29T11:00:00Z");
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
                ),
                () -> assertNull(booking.getActualStartTime()),
                () -> assertNull(booking.getActualFinishTime())
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
    void marksBookingActivatedAtStart () {
        Booking booking = newBooking();
        booking.activate(START);

        assertAll(
                () -> assertEquals(BookingStatus.ACTIVATED, booking.getStatus()),
                () -> assertEquals(START, booking.getActualStartTime()),
                () -> assertNull(booking.getActualFinishTime())
        );
    }

    @Test
    void marksBookingActivatedDuringGracePeriod () {
        Booking booking = newBooking();
        booking.activate(DURING_GRACE_PERIOD);

        assertAll(
                () -> assertEquals(BookingStatus.ACTIVATED, booking.getStatus()),
                () -> assertEquals(DURING_GRACE_PERIOD, booking.getActualStartTime()),
                () -> assertNull(booking.getActualFinishTime())
        );
    }

    @Test
    void rejectsUseBeforeBookingStart () {
        Booking booking = newBooking();

        assertThrows(IllegalStateException.class, () -> booking.activate(START.minusNanos(1)));
        assertConfirmed(booking);
    }

    @Test
    void rejectsUseAtCheckInDeadline () {
        Booking booking = newBooking();

        assertThrows(IllegalStateException.class, () -> booking.activate(CHECK_IN_DEADLINE));
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
    void completesActivatedBooking () {
        Booking booking = newBooking();
        booking.activate(DURING_GRACE_PERIOD);

        booking.complete(COMPLETION_TIME);

        assertAll(
                () -> assertEquals(BookingStatus.COMPLETED, booking.getStatus()),
                () -> assertEquals(DURING_GRACE_PERIOD, booking.getActualStartTime()),
                () -> assertEquals(COMPLETION_TIME, booking.getActualFinishTime())
        );
    }

    @Test
    void rejectsCompletionBeforeActivation () {
        Booking booking = newBooking();

        assertThrows(IllegalStateException.class, () -> booking.complete(COMPLETION_TIME));
        assertConfirmed(booking);
    }

    @Test
    void rejectsCompletionNotAfterActivation () {
        Booking booking = newBooking();
        booking.activate(DURING_GRACE_PERIOD);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> booking.complete(DURING_GRACE_PERIOD)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> booking.complete(DURING_GRACE_PERIOD.minusNanos(1))
                )
        );

        assertAll(
                () -> assertEquals(BookingStatus.ACTIVATED, booking.getStatus()),
                () -> assertNull(booking.getActualFinishTime())
        );
    }

    @Test
    void rejectsNullCurrentTimesWithoutChangingBookings () {
        Booking cancellation = newBooking();
        Booking usage = newBooking();
        Booking expiration = newBooking();
        Booking completion = newBooking();
        completion.activate(DURING_GRACE_PERIOD);

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> cancellation.cancel(null)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> usage.activate(null)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> expiration.expire(null)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> completion.complete(null)
                )
        );

        assertAll(
                () -> assertConfirmed(cancellation),
                () -> assertConfirmed(usage),
                () -> assertConfirmed(expiration),
                () -> assertEquals(BookingStatus.ACTIVATED, completion.getStatus()),
                () -> assertNull(completion.getActualFinishTime())
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
    void rejectsInvalidTransitionsAfterActivation () {
        Booking booking = newBooking();
        booking.activate(DURING_GRACE_PERIOD);

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.cancel(DURING_GRACE_PERIOD)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.activate(DURING_GRACE_PERIOD)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.expire(CHECK_IN_DEADLINE)
                )
        );
        assertEquals(BookingStatus.ACTIVATED, booking.getStatus());
    }

    @Test
    void rejectsTransitionsAfterExpiration () {
        Booking booking = newBooking();
        booking.expire(CHECK_IN_DEADLINE);

        assertAllTransitionsRejected(booking);
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    @Test
    void rejectsTransitionsAfterCompletion () {
        Booking booking = newBooking();
        booking.activate(DURING_GRACE_PERIOD);
        booking.complete(COMPLETION_TIME);

        assertAllTransitionsRejected(booking);
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    void restoresActivatedAndCompletedBookings () {
        Booking activated = existingBooking(BookingStatus.ACTIVATED, DURING_GRACE_PERIOD, null);
        Booking completed = existingBooking(
                BookingStatus.COMPLETED,
                DURING_GRACE_PERIOD,
                COMPLETION_TIME
        );

        assertAll(
                () -> assertEquals(BookingStatus.ACTIVATED, activated.getStatus()),
                () -> assertEquals(DURING_GRACE_PERIOD, activated.getActualStartTime()),
                () -> assertNull(activated.getActualFinishTime()),
                () -> assertEquals(BookingStatus.COMPLETED, completed.getStatus()),
                () -> assertEquals(DURING_GRACE_PERIOD, completed.getActualStartTime()),
                () -> assertEquals(COMPLETION_TIME, completed.getActualFinishTime())
        );
    }

    @Test
    void rejectsInconsistentExistingLifecycleTimes () {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> existingBooking(BookingStatus.ACTIVATED, null, null)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> existingBooking(BookingStatus.ACTIVATED, DURING_GRACE_PERIOD, COMPLETION_TIME)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> existingBooking(BookingStatus.COMPLETED, DURING_GRACE_PERIOD, null)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> existingBooking(
                                BookingStatus.COMPLETED,
                                DURING_GRACE_PERIOD,
                                DURING_GRACE_PERIOD
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> existingBooking(BookingStatus.CONFIRMED, DURING_GRACE_PERIOD, null)
                )
        );
    }

    private Booking newBooking () {
        return new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);
    }

    private Booking existingBooking (
            BookingStatus status,
            Instant actualStartTime,
            Instant actualFinishTime
    ) {
        return Booking.fromExistingState(
                BOOKING_ID,
                COMMUNITY_ID,
                SPOT_ID,
                RESIDENT_ID,
                VEHICLE_ID,
                TIME_RANGE,
                status,
                actualStartTime,
                actualFinishTime
        );
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
                        () -> booking.activate(DURING_GRACE_PERIOD)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.expire(CHECK_IN_DEADLINE)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> booking.complete(COMPLETION_TIME)
                )
        );
    }
}
