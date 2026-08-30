package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.application.ParkingSpotService;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingSpotController.class)
class ParkingSpotControllerTest {
    private static final UUID COMMUNITY_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID FIRST_SPOT_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID SECOND_SPOT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final Instant START = Instant.parse("2026-08-30T10:00:00Z");
    private static final Instant END = Instant.parse("2026-08-30T12:00:00Z");
    private static final TimeRange TIME_RANGE = new TimeRange(START, END);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingSpotService parkingSpotService;

    @Test
    void returnsAvailableParkingSpots () throws Exception {
        ParkingSpot firstSpot = new ParkingSpot(FIRST_SPOT_ID, COMMUNITY_ID, "A-01");
        ParkingSpot secondSpot = new ParkingSpot(SECOND_SPOT_ID, COMMUNITY_ID, "A-02");

        when(parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE))
                .thenReturn(List.of(firstSpot, secondSpot));

        mockMvc.perform(get("/api/communities/{communityId}/parking-spots/available", COMMUNITY_ID)
                        .param("start", START.toString())
                        .param("end", END.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(FIRST_SPOT_ID.toString()))
                .andExpect(jsonPath("$[0].communityId").value(COMMUNITY_ID.toString()))
                .andExpect(jsonPath("$[0].code").value("A-01"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value(SECOND_SPOT_ID.toString()))
                .andExpect(jsonPath("$[1].code").value("A-02"));

        verify(parkingSpotService).getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE);
    }

    @Test
    void returnsEmptyListWhenNoParkingSpotsAreAvailable () throws Exception {
        when(parkingSpotService.getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/communities/{communityId}/parking-spots/available", COMMUNITY_ID)
                        .param("start", START.toString())
                        .param("end", END.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(parkingSpotService).getAvailableParkingSpots(COMMUNITY_ID, TIME_RANGE);
    }

    @Test
    void rejectsInvalidTimeRangeBeforeCallingService () throws Exception {
        mockMvc.perform(get("/api/communities/{communityId}/parking-spots/available", COMMUNITY_ID)
                        .param("start", END.toString())
                        .param("end", START.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verifyNoInteractions(parkingSpotService);
    }
}
