ALTER TABLE bookings
    ADD COLUMN actual_start_time TIMESTAMPTZ,
    ADD COLUMN actual_finish_time TIMESTAMPTZ;

UPDATE bookings AS booking
SET actual_start_time = parking_session.started_at,
    actual_finish_time = parking_session.finished_at,
    status = CASE parking_session.status
                 WHEN 'FINISHED' THEN 'COMPLETED'
                 ELSE 'ACTIVATED'
             END
FROM parking_sessions AS parking_session
WHERE parking_session.booking_id = booking.id;

ALTER TABLE bookings
    ADD CONSTRAINT bookings_actual_lifecycle_times_valid
        CHECK (
            (
                status IN ('CONFIRMED', 'CANCELLED', 'EXPIRED')
                AND actual_start_time IS NULL
                AND actual_finish_time IS NULL
            )
            OR
            (
                status = 'ACTIVATED'
                AND actual_start_time IS NOT NULL
                AND actual_finish_time IS NULL
            )
            OR
            (
                status = 'COMPLETED'
                AND actual_start_time IS NOT NULL
                AND actual_finish_time IS NOT NULL
                AND actual_start_time < actual_finish_time
            )
        );

CREATE UNIQUE INDEX bookings_one_activated_per_spot
    ON bookings (spot_id)
    WHERE status = 'ACTIVATED';

DROP TABLE parking_sessions;
