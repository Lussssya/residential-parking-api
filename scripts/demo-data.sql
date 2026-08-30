-- Standalone local-demo seed data. This file is intentionally not a Flyway migration.
-- Run after the application has applied V1, for example:
--   docker compose exec -T postgres psql -U parking_user -d residential_parking < scripts/demo-data.sql

INSERT INTO communities (id, name)
VALUES ('10000000-0000-0000-0000-000000000001', 'Maple Grove Residents')
ON CONFLICT (id) DO NOTHING;

INSERT INTO residents (id, community_id, full_name)
VALUES (
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    'Alex Morgan'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO vehicles (id, resident_id, license_plate)
VALUES (
    '30000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000002',
    'DEMO-001'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO parking_spots (id, community_id, code, status)
VALUES
    ('40000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 'A-01', 'ACTIVE'),
    ('50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', 'A-02', 'ACTIVE'),
    ('60000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', 'A-03', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
