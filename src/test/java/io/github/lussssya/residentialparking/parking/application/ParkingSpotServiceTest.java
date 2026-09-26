package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSpotServiceTest {
    private static final UUID COMMUNITY_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID FIRST_SPOT_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID THIRD_SPOT_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final TimeRange TIME_RANGE = new TimeRange(
            Instant.parse("2026-08-30T10:00:00Z"),
            Instant.parse("2026-08-30T12:00:00Z")
    );

    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @InjectMocks
    private ParkingSpotService parkingSpotService;

    @Test
    void returnsOnlyAvailableParkingSpotsForCommunity () {
        ParkingSpot firstSpot = new ParkingSpot(FIRST_SPOT_ID, COMMUNITY_ID, "A-1");
        ParkingSpot thirdSpot = new ParkingSpot(THIRD_SPOT_ID, COMMUNITY_ID, "A-3");

        when(parkingSpotRepository.findAvailableByCommunityId(COMMUNITY_ID, TIME_RANGE))
                .thenReturn(List.of(firstSpot, thirdSpot));

        List<ParkingSpot> result = parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE);

        assertEquals(List.of(firstSpot, thirdSpot), result);

        verify(parkingSpotRepository).findAvailableByCommunityId(COMMUNITY_ID, TIME_RANGE);
    }

    @Test
    void returnsEmptyListWhenCommunityHasNoParkingSpots () {
        when(parkingSpotRepository.findAvailableByCommunityId(COMMUNITY_ID, TIME_RANGE))
                .thenReturn(List.of());

        List<ParkingSpot> result = parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE);

        assertEquals(List.of(), result);

        verify(parkingSpotRepository).findAvailableByCommunityId(COMMUNITY_ID, TIME_RANGE);
    }

    @Test
    void rejectsNullCommunityIdBeforeUsingDependencies () {
        assertThrows(
                NullPointerException.class,
                () -> parkingSpotService.getAvailableParkingSpots(null, TIME_RANGE)
        );

        verifyNoInteractions(parkingSpotRepository);
    }

    @Test
    void rejectsNullTimeRangeBeforeUsingDependencies () {
        assertThrows(
                NullPointerException.class,
                () -> parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, null)
        );

        verifyNoInteractions(parkingSpotRepository);
    }
}
