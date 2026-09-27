package io.github.lussssya.residentialparking.parking.infrastructure.persistence;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.BookingStatus;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import io.github.lussssya.residentialparking.parking.infrastructure.persistence.booking.BookingJpaEntity;
import io.github.lussssya.residentialparking.parking.infrastructure.persistence.booking.SpringDataBookingRepository;
import io.github.lussssya.residentialparking.parking.infrastructure.persistence.spot.JpaParkingSpotRepositoryAdapter;
import io.github.lussssya.residentialparking.parking.infrastructure.persistence.spot.ParkingSpotJpaEntity;
import io.github.lussssya.residentialparking.parking.infrastructure.persistence.spot.SpringDataParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaParkingSpotRepositoryAdapter.class)
class ParkingSpotRepositoryIntegrationTest {
    private static final UUID SPOT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SPOT_ID_2 = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID COMMUNITY_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID RESIDENT_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID VEHICLE_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
    private static final UUID BOOKING_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
    private static final TimeRange REQUESTED_RANGE = new TimeRange(
            Instant.parse("2026-08-29T14:00:00Z"),
            Instant.parse("2026-08-29T16:00:00Z")
    );

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;
    @Autowired
    private SpringDataParkingSpotRepository springDataParkingSpotRepository;
    @Autowired
    private SpringDataBookingRepository springDataBookingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsActiveSpotWhenNoBookingOverlaps () {
        insertCommunity();

        ParkingSpot spot = new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-1");
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(spot));

        List<ParkingSpot> result = parkingSpotRepository.findAvailableByCommunityId(COMMUNITY_ID, REQUESTED_RANGE);

        assertEquals(1, result.size());
        assertEquals(SPOT_ID, result.get(0).getId());
    }

    @Test
    void excludesInoperativeSpot () {
        insertCommunity();

        ParkingSpot spot = new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-1");
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(spot));

        ParkingSpot inoperativeSpot = new ParkingSpot(SPOT_ID_2, COMMUNITY_ID, "A-2");
        inoperativeSpot.markInoperative();
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(inoperativeSpot));

        List<ParkingSpot> result = parkingSpotRepository.findAvailableByCommunityId(COMMUNITY_ID, REQUESTED_RANGE);

        assertEquals(1, result.size());
        assertEquals(SPOT_ID, result.get(0).getId());
    }

    @ParameterizedTest
    @EnumSource(
            value = BookingStatus.class,
            names = {"CONFIRMED", "ACTIVATED"}
    )
    void excludesSpotWithOverlappingBlockingBooking (BookingStatus status) {
        insertCommunity();
        insertResidentAndVehicle();

        ParkingSpot blockedSpot = new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-1");
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(blockedSpot));

        ParkingSpot availableSpot = new ParkingSpot(SPOT_ID_2, COMMUNITY_ID, "A-2");
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(availableSpot));

        Booking booking = new Booking(
                BOOKING_ID,
                COMMUNITY_ID,
                SPOT_ID,
                RESIDENT_ID,
                VEHICLE_ID,
                REQUESTED_RANGE
        );

        if (status == BookingStatus.ACTIVATED) {
            booking.activate(REQUESTED_RANGE.start().plus(5, ChronoUnit.MINUTES));
        }

        springDataBookingRepository.saveAndFlush(BookingJpaEntity.fromDomain(booking));

        List<ParkingSpot> result = parkingSpotRepository.findAvailableByCommunityId(COMMUNITY_ID, REQUESTED_RANGE);

        assertEquals(1, result.size());
        assertEquals(SPOT_ID_2, result.get(0).getId());
    }

    @Test
    void returnsSpotWithOverlappingCancelledBooking () {
        insertCommunity();
        insertResidentAndVehicle();

        ParkingSpot firstSpot = new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-1");
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(firstSpot));

        ParkingSpot secondSpot = new ParkingSpot(SPOT_ID_2, COMMUNITY_ID, "A-2");
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(secondSpot));

        Booking booking = new Booking(
                BOOKING_ID,
                COMMUNITY_ID,
                SPOT_ID,
                RESIDENT_ID,
                VEHICLE_ID,
                REQUESTED_RANGE
        );

        booking.cancel(REQUESTED_RANGE.start().plus(5, ChronoUnit.MINUTES));

        springDataBookingRepository.saveAndFlush(BookingJpaEntity.fromDomain(booking));

        List<ParkingSpot> result = parkingSpotRepository.findAvailableByCommunityId(COMMUNITY_ID, REQUESTED_RANGE);

        assertEquals(2, result.size());
        assertEquals(SPOT_ID, result.get(0).getId());
        assertEquals(SPOT_ID_2, result.get(1).getId());
    }

    @Test
    void returnsSpotWhenExistingBookingEndsAtRequestedStart() {
        insertCommunity();
        insertResidentAndVehicle();

        ParkingSpot spot = new ParkingSpot(SPOT_ID, COMMUNITY_ID, "A-1");
        springDataParkingSpotRepository.save(ParkingSpotJpaEntity.fromDomain(spot));

        TimeRange existingRange = new TimeRange(REQUESTED_RANGE.start().minus(2, ChronoUnit.HOURS), REQUESTED_RANGE.start());

        Booking booking = new Booking(
                BOOKING_ID,
                COMMUNITY_ID,
                SPOT_ID,
                RESIDENT_ID,
                VEHICLE_ID,
                existingRange
        );

        springDataBookingRepository.saveAndFlush(BookingJpaEntity.fromDomain(booking));

        List<ParkingSpot> result =
                parkingSpotRepository.findAvailableByCommunityId(
                        COMMUNITY_ID,
                        REQUESTED_RANGE
                );

        assertEquals(1, result.size());
        assertEquals(SPOT_ID, result.get(0).getId());
    }

    private void insertCommunity () {
        jdbcTemplate.update(
                "INSERT INTO communities (id, name) VALUES (?, ?)",
                COMMUNITY_ID,
                "Test Community"
        );
    }

    private void insertResidentAndVehicle () {
        jdbcTemplate.update(
                """
                        INSERT INTO residents (id, community_id, full_name)
                        VALUES (?, ?, ?)
                        """,
                RESIDENT_ID,
                COMMUNITY_ID,
                "Test Resident"
        );

        jdbcTemplate.update(
                """
                        INSERT INTO vehicles (id, resident_id, license_plate)
                        VALUES (?, ?, ?)
                        """,
                VEHICLE_ID,
                RESIDENT_ID,
                "TEST-001"
        );
    }
}
