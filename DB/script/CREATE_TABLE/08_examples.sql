-- Example: create a stream, append an event, and advance version
WITH s AS (
  INSERT INTO R_streams(stream_id, stream_name, aggregate_id, aggregate_type, current_version)
  VALUES (gen_random_uuid(), CONCAT('Booking-', gen_random_uuid()), gen_random_uuid(), 'Booking', 0)
  RETURNING *
)
INSERT INTO I_events(event_id, stream_id, version, aggregate_id, aggregate_type, event_type, event_body, metadata)
SELECT gen_random_uuid(), s.stream_id, 1, s.aggregate_id, s.aggregate_type, 'BookingCreated',
       jsonb_build_object('studio_id','00000000-0000-0000-0000-000000000000','usage_date','2025-10-05'),
       jsonb_build_object('actor','ta-0001')
FROM s;
UPDATE R_streams SET current_version = 1 WHERE stream_id = (SELECT stream_id FROM R_streams ORDER BY created_at DESC LIMIT 1) AND current_version = 0;
