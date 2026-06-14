CREATE TABLE task_activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    task_id BIGINT NOT NULL,
    actor_id BIGINT NULL,
    type VARCHAR(50) NOT NULL,
    old_value VARCHAR(255) NULL,
    new_value VARCHAR(255) NULL,
    message VARCHAR(500) NULL,
    metadata VARCHAR(1000) NULL,
    CONSTRAINT pk_task_activities PRIMARY KEY (id),
    CONSTRAINT fk_task_activities_task
        FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_activities_actor
        FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX idx_task_activities_task_id_created_at ON task_activities (task_id, created_at);
CREATE INDEX idx_task_activities_actor_id ON task_activities (actor_id);
CREATE INDEX idx_task_activities_type ON task_activities (type);
