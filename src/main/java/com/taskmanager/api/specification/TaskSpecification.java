package com.taskmanager.api.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.taskmanager.api.entity.Task;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;

/*
* A Specification<T> is a function that tells Spring how to build a WHERE clause.
*/

public class TaskSpecification {

    // Builds a dynamic WHERE clause for Task queries.
    // Only adds conditions if the corresponding filter value is provided.
    // Used by the repository together with Pageable to apply filtering + pagination
    // + sorting.
    public static Specification<Task> filter(
            String q,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueBefore) {

        // Spring provides:
        // - root → represents the Task table
        // - query → the full query being built
        // - cb → CriteriaBuilder (used to construct SQL conditions)
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // If status filter is provided, add: WHERE status = ?
            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status));
            }

            // If priority filter is provided, add: WHERE priority = ?
            if (priority != null) {
                predicates.add(
                        cb.equal(root.get("priority"), priority));
            }

            // If dueBefore filter is provided, add: WHERE dueDate <= ?
            if (dueBefore != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("dueDate"), dueBefore));
            }

            // Basic search across multiple fields (OR condition)
            if (q != null && !q.isBlank()) {

                String pattern = "%" + q.toLowerCase() + "%";

                Predicate brandMatch = cb.like(cb.lower(root.get("title")), pattern);

                Predicate modelMatch = cb.like(cb.lower(root.get("description")), pattern);

                Predicate searchPredicate = cb.or(brandMatch, modelMatch);

                predicates.add(searchPredicate);
            }

            // Combine all added conditions with AND.
            // If no filters were provided, this returns all rows.
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
