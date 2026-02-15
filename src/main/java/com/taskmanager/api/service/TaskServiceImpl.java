package com.taskmanager.api.service;

import java.lang.StackWalker.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.TaskResponse;
import com.taskmanager.api.dto.TaskStatusPatch;
import com.taskmanager.api.dto.UpdateTaskRequest;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;
import com.taskmanager.api.mapper.TaskMapper;
import com.taskmanager.api.repository.TaskRepository;
import com.taskmanager.api.specification.TaskSpecification;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper mapper;

    @Override
    public TaskResponse createTask(CreateTaskRequest req) {

        // Convert request DTO into entity
        Task task = mapper.toEntity(req);
        // Persist entity
        Task saved = taskRepository.save(task);
        // Convert entity back to response DTO
        return mapper.toResponse(saved);
    }

    @Override
    public TaskResponse getTask(UUID id) {

        // Retrieve the task by its UUID.
        // If no task exists with the given ID, throw an exception to signal a 404
        // scenario.
        // findById returns an Optional
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        return mapper.toResponse(task);
    }

    @Override
    public Page<TaskResponse> listTasks(
            String q,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueBefore,
            Pageable pageable) {

        // Build dynamic filtering specification
        // Only non-null parameters are applied
        Specification<Task> spec = TaskSpecification.filter(q, status, priority, dueBefore);

        // Execute query with filtering + pagination
        Page<Task> page = taskRepository.findAll(spec, pageable);

        // Convert each entity to response DTO
        return page.map(mapper::toResponse);
    }

    @Override
    public TaskResponse updateTask(UUID id, UpdateTaskRequest req) {

        // Optional<Task> task = taskRepository.findById(id);
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        // update the task fields
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setStatus(req.getStatus());
        task.setPriority(req.getPriority());
        task.setDueDate(req.getDueDate());

        Task updated = taskRepository.save(task);

        return mapper.toResponse(updated);
    }

    @Override
    public TaskResponse updateTaskStatus(UUID id, TaskStatusPatch statusUpdate) {

        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(statusUpdate.getStatus());

        Task updated = taskRepository.save(task);

        return mapper.toResponse(updated);
    }

    @Override
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}
