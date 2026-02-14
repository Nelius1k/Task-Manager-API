package com.taskmanager.api.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.TaskResponse;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest req);

    TaskResponse getTask(UUID id);

    Page<TaskResponse> listTasks(String q, TaskStatus status, TaskPriority priority, LocalDate dueBefore,
            Pageable pageable);
}
