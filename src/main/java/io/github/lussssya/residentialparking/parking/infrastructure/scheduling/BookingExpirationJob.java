package io.github.lussssya.residentialparking.parking.infrastructure.scheduling;

import io.github.lussssya.residentialparking.parking.application.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class BookingExpirationJob {
    private final BookingService bookingService;
    private final Clock clock;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void expireBookings () {
        bookingService.expireOverdueBookings(Instant.now(clock));
    }
}
