package com.engine.taskmanagement.project.specification;

import com.engine.taskmanagement.project.dto.request.FilterProjectRequest;
import com.engine.taskmanagement.project.entity.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecification {

    public static Specification<Project> filter(FilterProjectRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (request == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String search = "%" + request.getSearch().trim().toLowerCase() + "%";

                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
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

            if (request.getOwnerId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("owner").get("id"), request.getOwnerId())
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

        };
    }
}
