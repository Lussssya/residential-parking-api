package io.github.lussssya.residentialparking.parking.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record TimeRange(Instant start, Instant end) {

    public TimeRange {
        Objects.requireNonNull(start, "Start time should not be null.");
        Objects.requireNonNull(end, "End time should not be null.");

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time should be before end time.");
        }
    }

    public boolean overlaps (TimeRange other) {
        Objects.requireNonNull(other, "Other time range should not be null.");

        return other.start().isBefore(end) && start.isBefore(other.end());
    }

    public Duration duration () {
        return Duration.between(start, end);
    }
}
