package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.application.ResidentBookings;
import io.github.lussssya.residentialparking.parking.domain.model.Booking;

import java.util.ArrayList;
import java.util.List;

public record ResidentBookingsResponse(List<BookingResponse> current, List<BookingResponse> future) {
    public static ResidentBookingsResponse from (ResidentBookings residentBookings) {
        List<BookingResponse> current = new ArrayList<>();
        for (Booking booking : residentBookings.current()) {
            current.add(BookingResponse.from(booking));
        }

        List<BookingResponse> future = new ArrayList<>();
        for (Booking booking : residentBookings.future()) {
            future.add(BookingResponse.from(booking));
        }
        return new ResidentBookingsResponse(current, future);
    }
}
