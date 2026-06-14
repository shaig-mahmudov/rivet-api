package com.engine.taskmanagement.task.criteria.repository;

import com.engine.taskmanagement.task.criteria.entity.AcceptanceCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcceptanceCriteriaRepository extends JpaRepository<AcceptanceCriteria, Long> {

    List<AcceptanceCriteria> findByTaskIdOrderByCreatedAtAscIdAsc(Long taskId);

    Optional<AcceptanceCriteria> findByIdAndTaskId(Long id, Long taskId);

    boolean existsByTaskIdAndCompletedFalse(Long taskId);
}
