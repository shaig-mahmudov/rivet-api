package com.engine.taskmanagement.task.specification;

import com.engine.taskmanagement.task.dto.request.FilterTaskRequest;
import com.engine.taskmanagement.task.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filter(FilterTaskRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDate today = LocalDate.now();

            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (request.getStatus() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("status"), request.getStatus())
                );
            }

            if (request.getPriority() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("priority"), request.getPriority())
                );
            }

            if (request.getProjectId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("project").get("id"), request.getProjectId())
                );
            }

            if (Boolean.TRUE.equals(request.getDueFromToday()) && request.getDueDateTo() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), today)
                );

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), request.getDueDateTo())
                );
            } else {

                if (request.getDueDateFrom() != null) {
                    predicates.add(
                            criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), request.getDueDateFrom())
                    );
                }

                if (request.getDueDateTo() != null) {
                    predicates.add(
                            criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), request.getDueDateTo())
                    );
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}