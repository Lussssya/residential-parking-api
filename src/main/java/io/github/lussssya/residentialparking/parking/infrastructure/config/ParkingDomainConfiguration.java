package io.github.lussssya.residentialparking.parking.infrastructure.config;

import io.github.lussssya.residentialparking.parking.domain.repository.BookingRepository;
import io.github.lussssya.residentialparking.parking.domain.service.ParkingAvailabilityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ParkingDomainConfiguration {

    @Bean
    public ParkingAvailabilityService parkingAvailabilityService (BookingRepository bookingRepository) {
        return new ParkingAvailabilityService(bookingRepository);
    }

    @Bean
    public Clock clock () {
        return Clock.systemUTC();
    }
}
