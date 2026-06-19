CREATE INDEX idx_tasks_due_date ON tasks (due_date);
CREATE INDEX idx_tasks_due_date_deleted_at ON tasks (due_date, deleted_at);
