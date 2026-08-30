package io.github.lussssya.residentialparking.parking.application;

import io.github.lussssya.residentialparking.parking.domain.model.Booking;

import java.util.List;
import java.util.Objects;

public record ResidentBookings(
        List<Booking> current,
        List<Booking> future
) {
    public ResidentBookings {
        current = List.copyOf(Objects.requireNonNull(current, "Current bookings should not be null."));
        future = List.copyOf(Objects.requireNonNull(future, "Future bookings should not be null."));
    }
}