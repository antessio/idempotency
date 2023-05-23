CREATE TABLE idempotency_key
(
    id     TEXT PRIMARY KEY,
    target JSONB,
    creation_date timestamp
);

CREATE INDEX idx_creation_date ON idempotency_key (creation_date);
