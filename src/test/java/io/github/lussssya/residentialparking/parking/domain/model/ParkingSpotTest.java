package io.github.lussssya.residentialparking.parking.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingSpotTest {
    private static final UUID SPOT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COMMUNITY_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");

    @Test
    void createsActiveParkingSpot () {
        ParkingSpot spot = new ParkingSpot(SPOT_ID, COMMUNITY_ID, " A-12 ");

        assertAll(
                () -> assertEquals(SPOT_ID, spot.getId()),
                () -> assertEquals(COMMUNITY_ID, spot.getCommunityId()),
                () -> assertEquals("A-12", spot.getCode()),
                () -> assertEquals(ParkingSpotStatus.ACTIVE, spot.getStatus())
        );
    }

    @Test
    void rejectsInvalidConstructorArguments () {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSpot(null, COMMUNITY_ID, "A-12")
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSpot(SPOT_ID, null, "A-12")
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ParkingSpot(SPOT_ID, COMMUNITY_ID, null)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ParkingSpot(SPOT_ID, COMMUNITY_ID, "   ")
                )
        );
    }

    @Test
    void marksActiveParkingSpotAsInoperative () {
        ParkingSpot spot = newParkingSpot();
        spot.markInoperative();

        assertEquals(ParkingSpotStatus.INOPERATIVE, spot.getStatus());
    }

    @Test
    void reactivatesInoperativeParkingSpot () {
        ParkingSpot spot = newParkingSpot();
        spot.markInoperative();
        spot.activate();

        assertEquals(ParkingSpotStatus.ACTIVE, spot.getStatus());
    }

    @Test
    void rejectsActivatingActiveParkingSpot () {
        ParkingSpot spot = newParkingSpot();

        assertThrows(IllegalStateException.class, spot::activate);

        assertEquals(ParkingSpotStatus.ACTIVE, spot.getStatus());
    }

    @Test
    void rejectsMarkingInoperativeParkingSpotAgain () {
        ParkingSpot spot = newParkingSpot();
        spot.markInoperative();

        assertThrows(IllegalStateException.class, spot::markInoperative);

        assertEquals(ParkingSpotStatus.INOPERATIVE, spot.getStatus());
    }

    private ParkingSpot newParkingSpot () {
        return new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-12");
    }
}
