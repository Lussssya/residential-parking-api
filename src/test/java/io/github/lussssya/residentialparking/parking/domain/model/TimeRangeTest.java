package io.github.lussssya.residentialparking.parking.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeRangeTest {
    private static final Instant NINE_AM =
            Instant.parse("2026-08-28T09:00:00Z");

    private static final Instant TEN_AM =
            Instant.parse("2026-08-28T10:00:00Z");

    private static final Instant ELEVEN_AM =
            Instant.parse("2026-08-28T11:00:00Z");

    private static final Instant NOON =
            Instant.parse("2026-08-28T12:00:00Z");

    private static final Instant ONE_PM =
            Instant.parse("2026-08-28T13:00:00Z");

    private static final Instant TWO_PM =
            Instant.parse("2026-08-28T14:00:00Z");

    @Test
    void createsValidTimeRange () {
        TimeRange range = new TimeRange(TEN_AM, NOON);

        assertEquals(TEN_AM, range.start());
        assertEquals(NOON, range.end());
    }

    @Test
    void rejectsNullStart () {
        assertThrows(
                NullPointerException.class,
                () -> new TimeRange(null, NOON)
        );
    }

    @Test
    void rejectsNullEnd () {
        assertThrows(
                NullPointerException.class,
                () -> new TimeRange(TEN_AM, null)
        );
    }

    @Test
    void rejectsEqualStartAndEnd () {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TimeRange(TEN_AM, TEN_AM)
        );
    }

    @Test
    void rejectsStartAfterEnd () {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TimeRange(NOON, TEN_AM)
        );
    }

    @Test
    void detectsPartialOverlap () {
        TimeRange first = new TimeRange(TEN_AM, NOON);
        TimeRange second = new TimeRange(ELEVEN_AM, ONE_PM);

        assertTrue(first.overlaps(second));
        assertTrue(second.overlaps(first));
    }

    @Test
    void detectsContainedRange () {
        TimeRange outer = new TimeRange(NINE_AM, TWO_PM);
        TimeRange inner = new TimeRange(TEN_AM, NOON);

        assertTrue(outer.overlaps(inner));
        assertTrue(inner.overlaps(outer));
    }

    @Test
    void rejectsSeparateRanges () {
        TimeRange first = new TimeRange(TEN_AM, NOON);
        TimeRange second = new TimeRange(ONE_PM, TWO_PM);

        assertFalse(first.overlaps(second));
        assertFalse(second.overlaps(first));
    }

    @Test
    void treatsAdjacentRangesAsNonOverlapping () {
        TimeRange first = new TimeRange(TEN_AM, NOON);
        TimeRange second = new TimeRange(NOON, ONE_PM);

        assertFalse(first.overlaps(second));
        assertFalse(second.overlaps(first));
    }

    @Test
    void calculatesDuration () {
        TimeRange range = new TimeRange(TEN_AM, NOON);

        assertEquals(Duration.ofHours(2), range.duration());
    }

    @Test
    void rejectsNullRangeWhenCheckingOverlap () {
        TimeRange range = new TimeRange(TEN_AM, NOON);

        assertThrows(
                NullPointerException.class,
                () -> range.overlaps(null)
        );
    }
}
