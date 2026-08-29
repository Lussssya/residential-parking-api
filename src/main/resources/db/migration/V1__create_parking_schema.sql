CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE communities (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,

    CONSTRAINT communities_name_not_blank
        CHECK (btrim(name) <> '')
);


CREATE TABLE residents (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL,
    full_name VARCHAR(150) NOT NULL,

    CONSTRAINT residents_community_fk
        FOREIGN KEY (community_id)
        REFERENCES communities (id),

    CONSTRAINT residents_id_community_unique
        UNIQUE (id, community_id),

    CONSTRAINT residents_full_name_not_blank
        CHECK (btrim(full_name) <> '')
);


CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    resident_id UUID NOT NULL,
    license_plate VARCHAR(20) NOT NULL,

    CONSTRAINT vehicles_resident_fk
        FOREIGN KEY (resident_id)
        REFERENCES residents (id),

    CONSTRAINT vehicles_id_resident_unique
        UNIQUE (id, resident_id),

    CONSTRAINT vehicles_license_plate_not_blank
        CHECK (btrim(license_plate) <> '')
);


CREATE TABLE parking_spots (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT parking_spots_community_fk
        FOREIGN KEY (community_id)
        REFERENCES communities (id),

    CONSTRAINT parking_spots_community_code_unique
        UNIQUE (community_id, code),

    CONSTRAINT parking_spots_id_community_unique
        UNIQUE (id, community_id),

    CONSTRAINT parking_spots_code_not_blank
        CHECK (btrim(code) <> ''),

    CONSTRAINT parking_spots_status_valid
        CHECK (status IN ('ACTIVE', 'INOPERATIVE'))
);


CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL,
    spot_id UUID NOT NULL,
    resident_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT bookings_spot_community_fk
        FOREIGN KEY (spot_id, community_id)
        REFERENCES parking_spots (id, community_id),

    CONSTRAINT bookings_resident_community_fk
        FOREIGN KEY (resident_id, community_id)
        REFERENCES residents (id, community_id),

    CONSTRAINT bookings_vehicle_resident_fk
        FOREIGN KEY (vehicle_id, resident_id)
        REFERENCES vehicles (id, resident_id),

    CONSTRAINT bookings_id_spot_vehicle_unique
        UNIQUE (id, spot_id, vehicle_id),

    CONSTRAINT bookings_time_range_valid
        CHECK (start_time < end_time),

    CONSTRAINT bookings_status_valid
        CHECK (
            status IN ('CONFIRMED', 'USED', 'CANCELLED', 'EXPIRED')
        )
);


ALTER TABLE bookings
    ADD CONSTRAINT bookings_no_overlapping_active_times
    EXCLUDE USING gist (
        spot_id WITH =,
        tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (status IN ('CONFIRMED', 'USED'));


CREATE TABLE parking_sessions (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL,
    spot_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT parking_sessions_booking_unique
        UNIQUE (booking_id),

    CONSTRAINT parking_sessions_booking_fk
        FOREIGN KEY (booking_id, spot_id, vehicle_id)
        REFERENCES bookings (id, spot_id, vehicle_id),

    CONSTRAINT parking_sessions_status_valid
        CHECK (status IN ('ACTIVE', 'FINISHED')),

    CONSTRAINT parking_sessions_finish_state_valid
        CHECK (
            (
                status = 'ACTIVE'
                AND finished_at IS NULL
            )
            OR
            (
                status = 'FINISHED'
                AND finished_at IS NOT NULL
                AND started_at < finished_at
            )
        )
);


CREATE UNIQUE INDEX parking_sessions_one_active_per_spot
    ON parking_sessions (spot_id)
    WHERE status = 'ACTIVE';