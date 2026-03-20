package com.taskmanager.api.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.TaskStatusPatch;
import com.taskmanager.api.dto.TaskResponse;
import com.taskmanager.api.dto.UpdateTaskRequest;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;
import com.taskmanager.api.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> saveTask(@Valid @RequestBody CreateTaskRequest req) {
        // Delegate creation to service layer
        // Return 201 Created for REST compliance
        return new ResponseEntity<>(taskService.createTask(req), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID id) {
        // Fetch a single task by UUID
        // Service throws exception if not found
        return new ResponseEntity<>(taskService.getTask(id), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponse>> listTasks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,

            // Optional filtering by due date (ISO format: yyyy-MM-dd)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate dueBefore,

            // Pageable automatically extracts the info needed for paging
            // ?page=0&size=10&sort=createdAt,desc
            Pageable pageable) {

        // Delegate filtering + pagination to service
        Page<TaskResponse> tasks = taskService.listTasks(q, status, priority, dueBefore, pageable);

        // Return paginated result with 200 OK
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {

        return new ResponseEntity<>(taskService.updateTask(id, request), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable UUID id, @RequestBody TaskStatusPatch status) {

        return new ResponseEntity<>(taskService.updateTaskStatus(id, status), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponse> deleteTask(@PathVariable UUID id) {

        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
