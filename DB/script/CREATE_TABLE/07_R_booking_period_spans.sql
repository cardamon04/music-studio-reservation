CREATE TABLE IF NOT EXISTS R_booking_period_spans (
  aggregate_id    UUID NOT NULL,
  studio_id       UUID NOT NULL,
  usage_date      DATE NOT NULL,
  period_id       VARCHAR(10) NOT NULL,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (aggregate_id, period_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_r_bps_active
  ON R_booking_period_spans (studio_id, usage_date, period_id) WHERE is_active;
