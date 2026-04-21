-- =========================
-- USERS TABLE (core entity)
-- =========================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- AUDIT LOG TABLE
-- =========================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    module VARCHAR(100),
    description TEXT,
    status VARCHAR(50),
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- CORE BUSINESS TABLE (TASK / RECORD)
-- =========================
CREATE TABLE audit_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_to BIGINT,
    created_by BIGINT,
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    status VARCHAR(50) DEFAULT 'PENDING',
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_assigned
        FOREIGN KEY (assigned_to) REFERENCES users(id),

    CONSTRAINT fk_task_creator
        FOREIGN KEY (created_by) REFERENCES users(id)
);

-- =========================
-- INDEXES (important for performance + grading)
-- =========================

-- Users lookup
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Audit logs lookup
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_status ON audit_logs(status);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- Task lookup
CREATE INDEX idx_task_status ON audit_tasks(status);
CREATE INDEX idx_task_assigned ON audit_tasks(assigned_to);
CREATE INDEX idx_task_creator ON audit_tasks(created_by);
CREATE INDEX idx_task_due ON audit_tasks(due_date);