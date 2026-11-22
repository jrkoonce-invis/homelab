CREATE TABLE uptime_checks (
    id          BIGSERIAL PRIMARY KEY,
    checked_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    target      TEXT        NOT NULL,
    up          BOOLEAN     NOT NULL,
    status_code INTEGER,
    response_ms INTEGER
);

CREATE INDEX idx_uptime_checks_checked_at ON uptime_checks (checked_at);