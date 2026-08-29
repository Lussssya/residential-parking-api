package io.github.lussssya.residentialparking.parking.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingSessionTest {
    private static final UUID SESSION_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BOOKING_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID SPOT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID VEHICLE_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final Instant STARTED_AT = Instant.parse("2026-08-29T10:00:00Z");
    private static final Instant FINISHED_AT = Instant.parse("2026-08-29T12:00:00Z");

    @Test
    void createsActiveParkingSession () {
        ParkingSession session = newParkingSession();

        assertAll(
                () -> assertEquals(SESSION_ID, session.getId()),
                () -> assertEquals(BOOKING_ID, session.getBookingId()),
                () -> assertEquals(SPOT_ID, session.getSpotId()),
                () -> assertEquals(VEHICLE_ID, session.getVehicleId()),
                () -> assertEquals(STARTED_AT, session.getStartedAt()),
                () -> assertNull(session.getFinishedAt()),
                () -> assertEquals(ParkingSessionStatus.ACTIVE, session.getStatus()
                )
        );
    }

    @Test
    void rejectsNullConstructorArguments () {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSession(null, BOOKING_ID, SPOT_ID, VEHICLE_ID, STARTED_AT)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSession(SESSION_ID, null, SPOT_ID, VEHICLE_ID, STARTED_AT)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSession(SESSION_ID, BOOKING_ID, null, VEHICLE_ID, STARTED_AT)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSession(SESSION_ID, BOOKING_ID, SPOT_ID, null, STARTED_AT)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSession(SESSION_ID, BOOKING_ID, SPOT_ID, VEHICLE_ID, null)
                )
        );
    }

    @Test
    void finishesActiveParkingSession () {
        ParkingSession session = newParkingSession();
        session.finish(FINISHED_AT);

        assertAll(
                () -> assertEquals(ParkingSessionStatus.FINISHED, session.getStatus()),
                () -> assertEquals(FINISHED_AT, session.getFinishedAt())
        );
    }

    @Test
    void rejectsNullFinishTimeWithoutChangingSession () {
        ParkingSession session = newParkingSession();

        assertThrows(NullPointerException.class, () -> session.finish(null));
        assertActiveAndUnfinished(session);
    }

    @Test
    void rejectsFinishTimeEqualToStartWithoutChangingSession () {
        ParkingSession session = newParkingSession();

        assertThrows(IllegalArgumentException.class, () -> session.finish(STARTED_AT));
        assertActiveAndUnfinished(session);
    }

    @Test
    void rejectsFinishTimeBeforeStartWithoutChangingSession () {
        ParkingSession session = newParkingSession();
        Instant invalidFinishTime = STARTED_AT.minusSeconds(1);

        assertThrows(IllegalArgumentException.class, () -> session.finish(invalidFinishTime));
        assertActiveAndUnfinished(session);
    }

    @Test
    void rejectsFinishingSessionTwice () {
        ParkingSession session = newParkingSession();
        session.finish(FINISHED_AT);

        assertThrows(IllegalStateException.class, () -> session.finish(FINISHED_AT.plusSeconds(1)));

        assertAll(
                () -> assertEquals(ParkingSessionStatus.FINISHED, session.getStatus()),
                () -> assertEquals(FINISHED_AT, session.getFinishedAt())
        );
    }

    private ParkingSession newParkingSession () {
        return new ParkingSession(SESSION_ID, BOOKING_ID, SPOT_ID, VEHICLE_ID, STARTED_AT);
    }

    private void assertActiveAndUnfinished (ParkingSession session) {
        assertAll(
                () -> assertEquals(ParkingSessionStatus.ACTIVE, session.getStatus()),
                () -> assertNull(session.getFinishedAt())
        );
    }
}