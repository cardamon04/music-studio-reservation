CREATE TABLE R_projection_checkpoints (
  projector_name  VARCHAR(200) PRIMARY KEY,
  last_event_id   UUID,
  last_stream_id  UUID,
  last_version    BIGINT,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
