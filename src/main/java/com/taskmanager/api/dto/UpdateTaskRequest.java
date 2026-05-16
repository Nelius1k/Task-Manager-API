package com.taskmanager.api.dto;

import java.time.LocalDate;

import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body used to replace an existing task.")
/*
 * @Component cannot be used here because it is only used for
 * services, mappers, utilities, and configuration classes
 */
public class UpdateTaskRequest {

    @Schema(description = "Task title.", example = "Buy groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String title;

    @Schema(description = "Optional task details.", example = "Buy rice, milk, and eggs")
    private String description;

    @Schema(description = "Task status.", example = "IN_PROGRESS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    private TaskStatus status;

    @Schema(description = "Task priority.", example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @Schema(description = "Task due date.", example = "2026-04-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Priority is required")
    private LocalDate dueDate;

}
