CREATE TABLE idempotency_key
(
    id     TEXT PRIMARY KEY,
    target TEXT,
    expires_at timestamp
);

CREATE INDEX idx_creation_date ON idempotency_key (expires_at);
