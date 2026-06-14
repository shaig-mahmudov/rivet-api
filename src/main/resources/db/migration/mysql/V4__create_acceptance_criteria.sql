CREATE TABLE acceptance_criteria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    task_id BIGINT NOT NULL,
    text VARCHAR(500) NOT NULL,
    completed BIT NOT NULL DEFAULT 0,
    created_by_id BIGINT NULL,
    completed_at DATETIME(6) NULL,
    completed_by_id BIGINT NULL,
    CONSTRAINT pk_acceptance_criteria PRIMARY KEY (id),
    CONSTRAINT fk_acceptance_criteria_task
        FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_acceptance_criteria_created_by
        FOREIGN KEY (created_by_id) REFERENCES users (id),
    CONSTRAINT fk_acceptance_criteria_completed_by
        FOREIGN KEY (completed_by_id) REFERENCES users (id)
);

CREATE INDEX idx_acceptance_criteria_task_id ON acceptance_criteria (task_id);
CREATE INDEX idx_acceptance_criteria_created_by_id ON acceptance_criteria (created_by_id);
CREATE INDEX idx_acceptance_criteria_completed_by_id ON acceptance_criteria (completed_by_id);
CREATE INDEX idx_acceptance_criteria_task_completed ON acceptance_criteria (task_id, completed);
