ALTER TABLE tasks ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'CHORE';
ALTER TABLE tasks ADD COLUMN severity VARCHAR(50) NULL;
ALTER TABLE tasks ADD COLUMN technical_context VARCHAR(1000) NULL;
ALTER TABLE tasks ADD COLUMN expected_outcome VARCHAR(1000) NULL;

CREATE INDEX idx_tasks_type_deleted_at ON tasks (type, deleted_at);
CREATE INDEX idx_tasks_severity_deleted_at ON tasks (severity, deleted_at);
