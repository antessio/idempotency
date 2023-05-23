CREATE TABLE idempotency_key
(
    id     TEXT PRIMARY KEY,
    target JSONB
);