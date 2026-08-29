package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;
import io.github.lussssya.residentialparking.parking.domain.model.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID communityId,
        UUID spotId,
        UUID residentId,
        UUID vehicleId,
        Instant start,
        Instant end,
        Instant checkInDeadline,
        BookingStatus status
) {
    public static BookingResponse from (Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getCommunityId(),
                booking.getSpotId(),
                booking.getResidentId(),
                booking.getVehicleId(),
                booking.getTimeRange().start(),
                booking.getTimeRange().end(),
                booking.getCheckInDeadline(),
                booking.getStatus()
        );
    }
}
