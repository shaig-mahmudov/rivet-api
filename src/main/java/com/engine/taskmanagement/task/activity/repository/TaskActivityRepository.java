package com.engine.taskmanagement.task.activity.repository;

import com.engine.taskmanagement.task.activity.entity.TaskActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {

    Page<TaskActivity> findByTaskId(Long taskId, Pageable pageable);
}
