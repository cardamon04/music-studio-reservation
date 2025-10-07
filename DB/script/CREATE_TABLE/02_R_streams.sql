CREATE TABLE R_streams (
  stream_id        UUID PRIMARY KEY,
  stream_name      VARCHAR(200) UNIQUE NOT NULL,
  aggregate_id     UUID NOT NULL,
  aggregate_type   VARCHAR(100) NOT NULL,
  current_version  BIGINT NOT NULL DEFAULT 0,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_r_streams_aggregate ON R_streams(aggregate_type, aggregate_id);
