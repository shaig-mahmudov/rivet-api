package com.engine.taskmanagement.task.specification;

import com.engine.taskmanagement.task.dto.request.FilterTaskRequest;
import com.engine.taskmanagement.task.entity.Task;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filter(FilterTaskRequest request) {
        return buildFilter(request, false, null);
    }

    public static Specification<Task> visibleToUser(FilterTaskRequest request, Long userId) {
        return buildFilter(request, false, userId);
    }

    public static Specification<Task> deletedVisibleToUser(Long userId) {
        return buildFilter(null, true, userId);
    }

    public static Specification<Task> deleted() {
        return buildFilter(null, true, null);
    }

    private static Specification<Task> buildFilter(FilterTaskRequest request, boolean deleted, Long visibleUserId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDate today = LocalDate.now();

            predicates.add(deleted
                    ? criteriaBuilder.isNotNull(root.get("deletedAt"))
                    : criteriaBuilder.isNull(root.get("deletedAt")));

            if (visibleUserId != null) {
                Join<Object, Object> assignee = root.join("assignee", JoinType.LEFT);
                Join<Object, Object> project = root.join("project", JoinType.LEFT);
                Join<Object, Object> owner = project.join("owner", JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(assignee.get("id"), visibleUserId),
                        criteriaBuilder.equal(owner.get("id"), visibleUserId)
                ));
            }

            if (request == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String search = "%" + request.getSearch().trim().toLowerCase() + "%";

                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        search
                );

                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        search
                );

                predicates.add(
                        criteriaBuilder.or(titlePredicate, descriptionPredicate)
                );
            }

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

            if (request.getAssigneeId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("assignee").get("id"), request.getAssigneeId())
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
