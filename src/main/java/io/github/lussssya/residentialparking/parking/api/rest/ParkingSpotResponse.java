package io.github.lussssya.residentialparking.parking.api.rest;

import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpot;
import io.github.lussssya.residentialparking.parking.domain.model.ParkingSpotStatus;

import java.util.UUID;

public record ParkingSpotResponse(
        UUID id,
        UUID communityId,
        String code,
        ParkingSpotStatus status
) {
    public static ParkingSpotResponse from (ParkingSpot parkingSpot) {
        return new ParkingSpotResponse(
                parkingSpot.getId(),
                parkingSpot.getCommunityId(),
                parkingSpot.getCode(),
                parkingSpot.getStatus()
        );
    }
}
