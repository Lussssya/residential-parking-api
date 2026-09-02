ALTER TABLE bookings
DROP CONSTRAINT bookings_no_overlapping_active_times;

ALTER TABLE bookings
DROP CONSTRAINT bookings_status_valid;

UPDATE bookings
SET status = 'ACTIVATED'
WHERE status = 'USED';

ALTER TABLE bookings
    ADD CONSTRAINT bookings_status_valid
        CHECK (
            status IN (
                       'CONFIRMED',
                       'ACTIVATED',
                       'COMPLETED',
                       'CANCELLED',
                       'EXPIRED'
                )
            );

ALTER TABLE bookings
    ADD CONSTRAINT bookings_no_overlapping_active_times
    EXCLUDE USING gist (
        spot_id WITH =,
        tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (status IN ('CONFIRMED', 'ACTIVATED'));
