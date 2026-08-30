package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import io.github.lussssya.residentialparking.parking.domain.repository.ParkingSpotRepository;
import io.github.lussssya.residentialparking.parking.domain.service.ParkingAvailabilityService;
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
    private static final UUID SECOND_SPOT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID THIRD_SPOT_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final TimeRange TIME_RANGE = new TimeRange(
            Instant.parse("2026-08-30T10:00:00Z"),
            Instant.parse("2026-08-30T12:00:00Z")
    );

    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private ParkingAvailabilityService parkingAvailabilityService;
    @InjectMocks
    private ParkingSpotService parkingSpotService;

    @Test
    void returnsOnlyAvailableParkingSpotsForCommunity () {
        ParkingSpot firstSpot = new ParkingSpot(FIRST_SPOT_ID, COMMUNITY_ID, "A-1");
        ParkingSpot secondSpot = new ParkingSpot(SECOND_SPOT_ID, COMMUNITY_ID, "A-2");
        ParkingSpot thirdSpot = new ParkingSpot(THIRD_SPOT_ID, COMMUNITY_ID, "A-3");

        when(parkingSpotRepository.findAllByCommunityId(COMMUNITY_ID))
                .thenReturn(List.of(firstSpot, secondSpot, thirdSpot));

        when(parkingAvailabilityService.isAvailable(firstSpot, TIME_RANGE)).thenReturn(true);
        when(parkingAvailabilityService.isAvailable(secondSpot, TIME_RANGE)).thenReturn(false);
        when(parkingAvailabilityService.isAvailable(thirdSpot, TIME_RANGE)).thenReturn(true);

        List<ParkingSpot> result = parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE);

        assertEquals(List.of(firstSpot, thirdSpot), result);

        verify(parkingSpotRepository).findAllByCommunityId(COMMUNITY_ID);
        verify(parkingAvailabilityService).isAvailable(firstSpot, TIME_RANGE);
        verify(parkingAvailabilityService).isAvailable(secondSpot, TIME_RANGE);
        verify(parkingAvailabilityService).isAvailable(thirdSpot, TIME_RANGE);
    }

    @Test
    void returnsEmptyListWhenCommunityHasNoParkingSpots () {
        when(parkingSpotRepository.findAllByCommunityId(COMMUNITY_ID)).thenReturn(List.of());

        List<ParkingSpot> result = parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE);

        assertEquals(List.of(), result);

        verify(parkingSpotRepository).findAllByCommunityId(COMMUNITY_ID);

        verifyNoInteractions(parkingAvailabilityService);
    }

    @Test
    void rejectsNullCommunityIdBeforeUsingDependencies () {
        assertThrows(
                NullPointerException.class,
                () -> parkingSpotService.getAvailableParkingSpots(null, TIME_RANGE)
        );

        verifyNoInteractions(parkingSpotRepository, parkingAvailabilityService);
    }

    @Test
    void rejectsNullTimeRangeBeforeUsingDependencies () {
        assertThrows(
                NullPointerException.class,
                () -> parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, null)
        );

        verifyNoInteractions(parkingSpotRepository, parkingAvailabilityService);
    }
}
