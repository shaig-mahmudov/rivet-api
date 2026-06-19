package com.engine.taskmanagement.task.repository;

import com.engine.taskmanagement.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.Nullable;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @EntityGraph(attributePaths = {"project", "assignee"})
    Optional<Task> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"project", "assignee"})
    Page<Task> findAllByDeletedAtIsNotNull(Pageable pageable);

    @EntityGraph(attributePaths = {"project", "assignee"})
    Optional<Task> findByIdAndDeletedAtIsNotNull(Long id);

    @Override
    @EntityGraph(attributePaths = {"project", "assignee"})
    Page<Task> findAll(@Nullable Specification<Task> spec, Pageable pageable);
}
