package io.github.lussssya.residentialparking.parking.api.rest;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record BookingRequest(
        @NotNull UUID communityId,
        @NotNull UUID spotId,
        @NotNull UUID residentId,
        @NotNull UUID vehicleId,
        @NotNull Instant start,
        @NotNull Instant end
) {
}
