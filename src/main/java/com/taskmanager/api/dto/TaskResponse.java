package com.taskmanager.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class TaskResponse {

    private UUID id;

    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private Instant createdAt;

    private Instant updatedAt;
}
