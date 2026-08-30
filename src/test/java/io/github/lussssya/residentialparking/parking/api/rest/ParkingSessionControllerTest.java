package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.application.ParkingSessionService;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingSessionController.class)
class ParkingSessionControllerTest {
    private static final UUID SESSION_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BOOKING_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID SPOT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID VEHICLE_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

    private static final Instant SESSION_START = Instant.parse("2026-08-30T10:05:00Z");
    private static final Instant SESSION_FINISH = Instant.parse("2026-08-30T11:00:00Z");

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ParkingSessionService parkingSessionService;
    @MockitoBean
    private Clock clock;

    @Test
    void startsParkingSession () throws Exception {
        ParkingSession parkingSession = new ParkingSession(
                SESSION_ID,
                BOOKING_ID,
                SPOT_ID,
                VEHICLE_ID,
                SESSION_START
        );

        when(clock.instant()).thenReturn(SESSION_START);
        when(parkingSessionService.startSession(BOOKING_ID, SESSION_START)).thenReturn(parkingSession);

        mockMvc.perform(post(
                        "/api/bookings/{bookingId}/parking-session",
                        BOOKING_ID
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.bookingId").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$.spotId").value(SPOT_ID.toString()))
                .andExpect(jsonPath("$.vehicleId").value(VEHICLE_ID.toString()))
                .andExpect(jsonPath("$.startedAt").value(SESSION_START.toString()))
                .andExpect(jsonPath("$.finishedAt").value(nullValue()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(parkingSessionService).startSession(BOOKING_ID, SESSION_START);
    }

    @Test
    void finishesParkingSession () throws Exception {
        ParkingSession parkingSession = new ParkingSession(
                SESSION_ID,
                BOOKING_ID,
                SPOT_ID,
                VEHICLE_ID,
                SESSION_START
        );
        parkingSession.finish(SESSION_FINISH);

        when(clock.instant()).thenReturn(SESSION_FINISH);
        when(parkingSessionService.finishSession(SESSION_ID, SESSION_FINISH)).thenReturn(parkingSession);

        mockMvc.perform(post(
                        "/api/parking-sessions/{sessionId}/release",
                        SESSION_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.bookingId").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$.spotId").value(SPOT_ID.toString()))
                .andExpect(jsonPath("$.vehicleId").value(VEHICLE_ID.toString()))
                .andExpect(jsonPath("$.startedAt").value(SESSION_START.toString()))
                .andExpect(jsonPath("$.finishedAt").value(SESSION_FINISH.toString()))
                .andExpect(jsonPath("$.status").value("FINISHED"));

        verify(parkingSessionService).finishSession(SESSION_ID, SESSION_FINISH);
    }

    @Test
    void rejectsInvalidBookingId () throws Exception {
        mockMvc.perform(post(
                        "/api/bookings/{bookingId}/parking-session",
                        "not-a-uuid"
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parkingSessionService);
    }

    @Test
    void rejectsInvalidSessionId () throws Exception {
        mockMvc.perform(post(
                        "/api/parking-sessions/{sessionId}/release",
                        "not-a-uuid"
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parkingSessionService);
    }
}
