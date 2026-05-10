CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100),
    entity_id BIGINT,
    action VARCHAR(10),
    old_value TEXT,
    new_value TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'audit_log' AND column_name = 'entity_type'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'audit_log' AND column_name = 'entity_name'
    ) THEN
        EXECUTE 'ALTER TABLE audit_log RENAME COLUMN entity_type TO entity_name';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'audit_log' AND column_name = 'entity_name'
    ) THEN
        EXECUTE 'ALTER TABLE audit_log ADD COLUMN entity_name VARCHAR(100)';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'audit_log' AND column_name = 'entity_id'
    ) THEN
        EXECUTE 'ALTER TABLE audit_log ADD COLUMN entity_id BIGINT';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'audit_log' AND column_name = 'action'
    ) THEN
        EXECUTE 'ALTER TABLE audit_log ADD COLUMN action VARCHAR(10)';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'audit_log' AND column_name = 'old_value'
    ) THEN
        EXECUTE 'ALTER TABLE audit_log ADD COLUMN old_value TEXT';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'audit_log' AND column_name = 'new_value'
    ) THEN
        EXECUTE 'ALTER TABLE audit_log ADD COLUMN new_value TEXT';
    END IF;
END $$;

ALTER TABLE audit_log
    ALTER COLUMN entity_name SET NOT NULL,
    ALTER COLUMN entity_id SET NOT NULL,
    ALTER COLUMN action SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_audit_log_action'
    ) THEN
        EXECUTE 'ALTER TABLE audit_log ADD CONSTRAINT chk_audit_log_action CHECK (action IN (''CREATE'', ''UPDATE'', ''DELETE''))';
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_audit_log_entity_name_id
    ON audit_log (entity_name, entity_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_changed_at
    ON audit_log (changed_at);
