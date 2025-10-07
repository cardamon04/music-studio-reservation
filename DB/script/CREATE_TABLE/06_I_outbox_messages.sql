CREATE TABLE I_outbox_messages (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_id       UUID NOT NULL REFERENCES I_events(event_id) ON DELETE CASCADE,
  topic          VARCHAR(200) NOT NULL,
  payload        JSONB NOT NULL,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at   TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_i_outbox_unpublished ON I_outbox_messages(published_at) WHERE published_at IS NULL;
