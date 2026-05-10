-- Add explicit admin and viewer users for Day 14 testing
INSERT INTO users (username, email, password, role, status, created_at, updated_at)
VALUES
  ('admin_user', 'admin_user@example.com', '$2b$10$1N7RB4PU3jmYpQHozwCsUedj9Itm3AaZoxjEz1pW8aY4OkhrqkNx.', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('viewer_user', 'viewer_user@example.com', '$2b$10$KyRCvt4ajQ5jP10IM59j3.bNAGxONwCoJRxsG5bESt/7cv5NovNVS', 'VIEWER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE
  SET email = EXCLUDED.email,
      password = EXCLUDED.password,
      role = EXCLUDED.role,
      status = EXCLUDED.status,
      updated_at = CURRENT_TIMESTAMP;

-- Add audit_log metadata columns required for Day 13/14 audit verification
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS performed_by VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS "timestamp" TIMESTAMP WITHOUT TIME ZONE GENERATED ALWAYS AS (changed_at) STORED;
