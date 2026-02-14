package com.taskmanager.api.service;

import org.springframework.stereotype.Service;

import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.TaskResponse;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.mapper.TaskMapper;
import com.taskmanager.api.repository.TaskRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    TaskRepository taskRepository;
    TaskMapper mapper;

    @Override
    public TaskResponse createTask(CreateTaskRequest req) {

        Task task = mapper.toEntity(req);
        Task saved = taskRepository.save(task);
        return mapper.toResponse(saved);
    }

}
