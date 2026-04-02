package com.taskmanager.api.dto;

import com.taskmanager.api.entity.TaskStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

// Enables a quick status update
public class TaskStatusPatch {

    @NotNull(message = "The status cannot be null")
    private TaskStatus status;
}
