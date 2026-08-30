package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.application.BookingService;
import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.TimeRange;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final Clock clock;

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking (@Valid @RequestBody BookingRequest request) {
        TimeRange timeRange = new TimeRange(request.start(), request.end());
        Booking booking = bookingService.createBooking(
                request.communityId(),
                request.spotId(),
                request.residentId(),
                request.vehicleId(),
                timeRange);

        return BookingResponse.from(booking);
    }

    @GetMapping("/residents/{residentId}/bookings")
    public ResidentBookingsResponse findCurrentAndFutureBookings (@PathVariable UUID residentId) {
        return ResidentBookingsResponse.from(
                bookingService.findCurrentAndFutureBookings(
                        residentId,
                        Instant.now(clock)
                )
        );
    }
}
