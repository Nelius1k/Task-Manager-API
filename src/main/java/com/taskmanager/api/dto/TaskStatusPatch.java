package com.taskmanager.api.dto;

import com.taskmanager.api.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body used to update only a task's status.")

// Enables a quick status update
public class TaskStatusPatch {

    @Schema(description = "New task status.", example = "DONE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "The status cannot be null")
    private TaskStatus status;
}
