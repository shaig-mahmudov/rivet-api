package com.engine.taskmanagement.task.repository;

import com.engine.taskmanagement.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedAtIsNull(Long id);

    Page<Task> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Task> findAllByDeletedAtIsNotNull(Pageable pageable);

    Optional<Task> findByIdAndDeletedAtIsNotNull(Long id);
}
