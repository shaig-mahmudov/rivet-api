package com.engine.taskmanagement.task.comment.repository;

import com.engine.taskmanagement.task.comment.entity.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    Page<TaskComment> findByTaskIdAndDeletedAtIsNull(Long taskId, Pageable pageable);

    Optional<TaskComment> findByIdAndTaskIdAndDeletedAtIsNull(Long id, Long taskId);
}
