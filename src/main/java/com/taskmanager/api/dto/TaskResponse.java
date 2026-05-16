package com.taskmanager.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@Schema(description = "Task returned by the API.")
public class TaskResponse {

    @Schema(description = "Task identifier.", example = "7d1f1f5e-8b8c-4c30-9c1c-9fd91a9e7a23")
    private UUID id;

    @Schema(description = "Task title.", example = "Buy groceries")
    private String title;

    @Schema(description = "Task details.", example = "Buy rice, milk, and eggs")
    private String description;

    @Schema(description = "Task status.", example = "TODO")
    private TaskStatus status;

    @Schema(description = "Task priority.", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Task due date.", example = "2026-04-10")
    private LocalDate dueDate;

    @Schema(description = "Timestamp when the task was created.", example = "2026-04-02T14:30:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the task was last updated.", example = "2026-04-02T14:30:00Z")
    private Instant updatedAt;
}
