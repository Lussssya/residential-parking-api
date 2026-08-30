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
import java.util.NoSuchElementException;
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid value for 'bookingId'."
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/bookings/not-a-uuid/parking-session"
                ));

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

    @Test
    void returnsBadRequestForIllegalArgumentException () throws Exception {
        when(clock.instant()).thenReturn(SESSION_START);
        when(parkingSessionService.startSession(BOOKING_ID, SESSION_START))
                .thenThrow(new IllegalArgumentException(
                        "Parking session cannot start at this time."
                ));

        mockMvc.perform(post(
                        "/api/bookings/{bookingId}/parking-session",
                        BOOKING_ID
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Parking session cannot start at this time."
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/bookings/" + BOOKING_ID + "/parking-session"
                ));
    }

    @Test
    void returnsNotFoundForMissingBooking () throws Exception {
        when(clock.instant()).thenReturn(SESSION_START);
        when(parkingSessionService.startSession(BOOKING_ID, SESSION_START))
                .thenThrow(new NoSuchElementException(
                        "Booking was not found."
                ));

        mockMvc.perform(post(
                        "/api/bookings/{bookingId}/parking-session",
                        BOOKING_ID
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Booking was not found."
                ));
    }

    @Test
    void returnsConflictWhenSessionCannotBeStarted () throws Exception {
        when(clock.instant()).thenReturn(SESSION_START);
        when(parkingSessionService.startSession(BOOKING_ID, SESSION_START))
                .thenThrow(new IllegalStateException(
                        "A parking session already exists for this booking."
                ));

        mockMvc.perform(post(
                        "/api/bookings/{bookingId}/parking-session",
                        BOOKING_ID
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "A parking session already exists for this booking."
                ));
    }
}
