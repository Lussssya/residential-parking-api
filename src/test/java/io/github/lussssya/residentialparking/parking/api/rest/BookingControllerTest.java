package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.application.BookingService;
import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final UUID BOOKING_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COMMUNITY_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID SPOT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID RESIDENT_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID VEHICLE_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
    private static final Instant START = Instant.parse("2026-08-30T10:00:00Z");
    private static final Instant END = Instant.parse("2026-08-30T12:00:00Z");
    private static final TimeRange TIME_RANGE = new TimeRange(START, END);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private Clock clock;

    @Test
    void createsBooking () throws Exception {
        Booking booking = new Booking(BOOKING_ID, COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);

        when(bookingService.createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE)).thenReturn(booking);

        String requestJson = """
                {
                  "communityId": "%s",
                  "spotId": "%s",
                  "residentId": "%s",
                  "vehicleId": "%s",
                  "start": "%s",
                  "end": "%s"
                }
                """.formatted(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, START, END);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$.communityId").value(COMMUNITY_ID.toString()))
                .andExpect(jsonPath("$.spotId").value(SPOT_ID.toString()))
                .andExpect(jsonPath("$.residentId").value(RESIDENT_ID.toString()))
                .andExpect(jsonPath("$.vehicleId").value(VEHICLE_ID.toString()))
                .andExpect(jsonPath("$.start").value(START.toString()))
                .andExpect(jsonPath("$.end").value(END.toString()))
                .andExpect(jsonPath("$.checkInDeadline").value("2026-08-30T10:15:00Z"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).createBooking(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, VEHICLE_ID, TIME_RANGE);
    }

    @Test
    void rejectsRequestWithMissingField () throws Exception {
        String requestJson = """
                {
                  "communityId": "%s",
                  "spotId": "%s",
                  "residentId": "%s",
                  "start": "%s",
                  "end": "%s"
                }
                """.formatted(COMMUNITY_ID, SPOT_ID, RESIDENT_ID, START, END);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }
}
