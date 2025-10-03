CREATE TABLE T_snapshots (
  aggregate_id    UUID PRIMARY KEY,
  aggregate_type  VARCHAR(100) NOT NULL,
  version         BIGINT NOT NULL,
  state           JSONB NOT NULL,
  taken_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
