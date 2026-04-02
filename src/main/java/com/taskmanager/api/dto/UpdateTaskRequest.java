package com.taskmanager.api.dto;

import java.time.LocalDate;

import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
 * @Component cannot be used here because it is only used for
 * services, mappers, utilities, and configuration classes
 */
public class UpdateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Priority is required")
    private LocalDate dueDate;

}
