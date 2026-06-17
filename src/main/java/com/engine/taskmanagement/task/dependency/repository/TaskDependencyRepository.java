package com.engine.taskmanagement.task.dependency.repository;

import com.engine.taskmanagement.task.dependency.entity.TaskDependency;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    boolean existsByTaskIdAndDependsOnTaskId(Long taskId, Long dependsOnTaskId);

    Optional<TaskDependency> findByTaskIdAndDependsOnTaskId(Long taskId, Long dependsOnTaskId);

    List<TaskDependency> findByTaskIdOrderByCreatedAtAscIdAsc(Long taskId);

    List<TaskDependency> findByDependsOnTaskIdOrderByCreatedAtAscIdAsc(Long dependsOnTaskId);

    List<TaskDependency> findByTaskId(Long taskId);

    List<TaskDependency> findByTaskIdIn(Collection<Long> taskIds);

    List<TaskDependency> findByTaskIdAndDependsOnTaskStatusNot(Long taskId, TaskStatus status);

    List<TaskDependency> findByDependsOnTaskStatusNotOrderByCreatedAtAscIdAsc(TaskStatus status);

    @Query("select distinct dependency.task from TaskDependency dependency " +
            "where dependency.dependsOnTask.status <> :status and dependency.task.deletedAt is null")
    Page<Task> findBlockedTasksByDependsOnTaskStatusNot(
            @Param("status") TaskStatus status,
            Pageable pageable
    );
}
