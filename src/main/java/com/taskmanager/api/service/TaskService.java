package com.taskmanager.api.service;

import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.TaskResponse;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest req);
}
