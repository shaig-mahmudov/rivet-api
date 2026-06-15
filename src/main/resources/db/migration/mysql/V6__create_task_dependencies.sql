CREATE TABLE task_dependencies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    task_id BIGINT NOT NULL,
    depends_on_task_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    CONSTRAINT pk_task_dependencies PRIMARY KEY (id),
    CONSTRAINT uk_task_dependencies_task_depends_on UNIQUE (task_id, depends_on_task_id),
    CONSTRAINT fk_task_dependencies_task
        FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_dependencies_depends_on_task
        FOREIGN KEY (depends_on_task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_dependencies_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE INDEX idx_task_dependencies_task_id ON task_dependencies (task_id);
CREATE INDEX idx_task_dependencies_depends_on_task_id ON task_dependencies (depends_on_task_id);
CREATE INDEX idx_task_dependencies_created_by ON task_dependencies (created_by);
