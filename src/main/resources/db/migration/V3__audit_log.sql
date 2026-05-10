CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,

    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,

    action VARCHAR(50),
    old_value TEXT,
    new_value TEXT,

    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Composite index for fast lookup by entity
CREATE INDEX idx_audit_entity 
ON audit_log(entity_type, entity_id);

-- Index for filtering by time (reports, history)
CREATE INDEX idx_audit_changed_at 
ON audit_log(changed_at);

-- Seed default RBAC users (idempotent)
INSERT INTO users (username, email, password, role, status, created_at, updated_at)
VALUES
('admin', 'admin@example.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy990qu', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('manager', 'manager@example.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy990qu', 'MANAGER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('viewer', 'viewer@example.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy990qu', 'VIEWER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;
