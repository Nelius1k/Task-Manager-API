package com.taskmanager.api.dto;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class CreateTaskRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;
}
