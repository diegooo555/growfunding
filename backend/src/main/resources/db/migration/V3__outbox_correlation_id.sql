-- Observability: the correlation id of the request that produced each event travels with it.
ALTER TABLE outbox_events ADD COLUMN correlation_id VARCHAR(100);
