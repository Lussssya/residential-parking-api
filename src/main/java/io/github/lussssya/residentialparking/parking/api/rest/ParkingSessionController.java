package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.application.ParkingSessionService;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ParkingSessionController {
    private final ParkingSessionService parkingSessionService;
    private final Clock clock;

    @PostMapping("/bookings/{bookingId}/parking-session")
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingSessionResponse startSession (@PathVariable UUID bookingId) {
        ParkingSession parkingSession = parkingSessionService.startSession(bookingId, Instant.now(clock));

        return ParkingSessionResponse.from(parkingSession);
    }

    @PostMapping("/parking-sessions/{sessionId}/release")
    public ParkingSessionResponse finishSession (@PathVariable UUID sessionId) {
        ParkingSession parkingSession = parkingSessionService.finishSession(sessionId, Instant.now(clock));

        return ParkingSessionResponse.from(parkingSession);
    }
}