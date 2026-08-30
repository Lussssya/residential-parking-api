package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.application.ParkingSpotService;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ParkingSpotController {
    private final ParkingSpotService parkingSpotService;

    @GetMapping("/communities/{communityId}/parking-spots/available")
    public List<ParkingSpotResponse> getAvailableParkingSpots (
            @PathVariable UUID communityId,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        final TimeRange timeRange = new TimeRange(start, end);

        List<ParkingSpot> parkingSpots = parkingSpotService.getAvailableParkingSpots(communityId, timeRange);
        List<ParkingSpotResponse> responses = new ArrayList<>();
        for (ParkingSpot spot : parkingSpots) {
            responses.add(ParkingSpotResponse.from(spot));
        }

        return responses;
    }
}
