CREATE TABLE I_events (
  event_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  stream_id       UUID NOT NULL REFERENCES R_streams(stream_id) ON DELETE CASCADE,
  version         BIGINT NOT NULL,
  aggregate_id    UUID NOT NULL,
  aggregate_type  VARCHAR(100) NOT NULL,
  event_type      event_type NOT NULL,
  event_body      JSONB  NOT NULL,
  metadata        JSONB  NOT NULL DEFAULT '{}'::jsonb,
  occurred_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_event_stream_version UNIQUE(stream_id, version)
);
CREATE INDEX IF NOT EXISTS idx_i_events_stream ON I_events(stream_id, version);
CREATE INDEX IF NOT EXISTS idx_i_events_aggregate ON I_events(aggregate_type, aggregate_id, version);
CREATE INDEX IF NOT EXISTS idx_i_events_type_time ON I_events(event_type, occurred_at);
