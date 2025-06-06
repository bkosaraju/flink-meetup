create or replace view if not exists event_data AS (
SELECT
COALESCE(COALESCE(JSON_VALUE(message, '$.id')), SHA2(message, 256)) AS id,
message AS message
FROM raw_events
);
