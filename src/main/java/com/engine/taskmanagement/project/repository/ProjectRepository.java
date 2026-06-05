package com.engine.taskmanagement.project.repository;

import com.engine.taskmanagement.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
    Page<Project> findAllByDeletedAtIsNotNull(Pageable pageable);
    Optional<Project> findByIdAndDeletedAtIsNotNull(Long id);
}
