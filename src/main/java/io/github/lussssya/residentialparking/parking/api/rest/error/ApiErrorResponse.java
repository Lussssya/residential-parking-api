package io.github.lussssya.residentialparking.parking.api.rest.error;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
