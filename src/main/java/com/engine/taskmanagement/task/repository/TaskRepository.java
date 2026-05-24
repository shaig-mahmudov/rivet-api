package com.engine.taskmanagement.task.repository;

import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTitle(String title);
    List<Task> findByTitleContainingIgnoreCase(String title);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(TaskPriority priority);
    List<Task> findAllByDeletedAtIsNull();
    Optional<Task> findByIdAndDeletedAtIsNull(Long id);
    List<Task> findAllByDeletedAtIsNotNull();

}
