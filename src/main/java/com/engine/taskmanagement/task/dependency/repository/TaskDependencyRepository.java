package com.engine.taskmanagement.task.dependency.repository;

import com.engine.taskmanagement.task.dependency.entity.TaskDependency;
import com.engine.taskmanagement.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    boolean existsByTaskIdAndDependsOnTaskId(Long taskId, Long dependsOnTaskId);

    Optional<TaskDependency> findByTaskIdAndDependsOnTaskId(Long taskId, Long dependsOnTaskId);

    List<TaskDependency> findByTaskIdOrderByCreatedAtAscIdAsc(Long taskId);

    List<TaskDependency> findByDependsOnTaskIdOrderByCreatedAtAscIdAsc(Long dependsOnTaskId);

    List<TaskDependency> findByTaskId(Long taskId);

    List<TaskDependency> findByTaskIdAndDependsOnTaskStatusNot(Long taskId, TaskStatus status);
}
