package com.taskmanager.api.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springdoc.core.annotations.ParameterObject;
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
import com.taskmanager.api.dto.ErrorResponse;
import com.taskmanager.api.dto.TaskPageResponse;
import com.taskmanager.api.dto.TaskStatusPatch;
import com.taskmanager.api.dto.TaskResponse;
import com.taskmanager.api.dto.UpdateTaskRequest;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;
import com.taskmanager.api.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Create, retrieve, search, update, and delete tasks.")
public class TaskController {

        private final TaskService taskService;

        @Operation(summary = "Create a task", description = "Creates a task. Status defaults to TODO and priority defaults to MEDIUM when omitted.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Task created", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping
        public ResponseEntity<TaskResponse> saveTask(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Task details.", required = true, content = @Content(schema = @Schema(implementation = CreateTaskRequest.class))) @Valid @RequestBody CreateTaskRequest req) {
                // Delegate creation to service layer
                // Return 201 Created for REST compliance
                return new ResponseEntity<>(taskService.createTask(req), HttpStatus.CREATED);
        }

        @Operation(summary = "Get a task", description = "Returns a single task by ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Task found", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid task ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<TaskResponse> getTask(
                        @Parameter(description = "Task ID.", example = "7d1f1f5e-8b8c-4c30-9c1c-9fd91a9e7a23") @PathVariable UUID id) {
                // Fetch a single task by UUID
                // Service throws exception if not found
                return new ResponseEntity<>(taskService.getTask(id), HttpStatus.OK);
        }

        @Operation(summary = "Search tasks", description = "Returns tasks matching optional filters with pagination and sorting.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Tasks returned", content = @Content(schema = @Schema(implementation = TaskPageResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/search")
        public ResponseEntity<Page<TaskResponse>> listTasks(
                        @Parameter(description = "Search term for title or description.", example = "groceries") @RequestParam(required = false) String q,
                        @Parameter(description = "Filter by status.", example = "TODO") @RequestParam(required = false) TaskStatus status,
                        @Parameter(description = "Filter by priority.", example = "HIGH") @RequestParam(required = false) TaskPriority priority,

                        // Optional filtering by due date (ISO format: yyyy-MM-dd)
                        @Parameter(description = "Return tasks due before this date.", example = "2026-04-29") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate dueBefore,

                        // Pageable automatically extracts the info needed for paging
                        // ?page=0&size=10&sort=createdAt,desc
                        @ParameterObject Pageable pageable) {

                // Delegate filtering + pagination to service
                Page<TaskResponse> tasks = taskService.listTasks(q, status, priority, dueBefore, pageable);

                // Return paginated result with 200 OK
                return new ResponseEntity<>(tasks, HttpStatus.OK);
        }

        @Operation(summary = "Update a task", description = "Replaces all editable fields for an existing task.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Task updated", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid task ID, request body, or validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PutMapping("/{id}")
        public ResponseEntity<TaskResponse> updateTask(
                        @Parameter(description = "Task ID.", example = "7d1f1f5e-8b8c-4c30-9c1c-9fd91a9e7a23") @PathVariable UUID id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated task details.", required = true, content = @Content(schema = @Schema(implementation = UpdateTaskRequest.class))) @Valid @RequestBody UpdateTaskRequest request) {

                return new ResponseEntity<>(taskService.updateTask(id, request), HttpStatus.OK);
        }

        @Operation(summary = "Update task status", description = "Updates only the status for an existing task.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Task status updated", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid task ID, request body, or validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PatchMapping("/{id}")
        public ResponseEntity<TaskResponse> updateTaskStatus(
                        @Parameter(description = "Task ID.", example = "7d1f1f5e-8b8c-4c30-9c1c-9fd91a9e7a23") @PathVariable UUID id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New task status.", required = true, content = @Content(schema = @Schema(implementation = TaskStatusPatch.class))) @Valid @RequestBody TaskStatusPatch status) {

                return new ResponseEntity<>(taskService.updateTaskStatus(id, status), HttpStatus.OK);
        }

        @Operation(summary = "Delete a task", description = "Deletes a task by ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Task deleted", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Invalid task ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<TaskResponse> deleteTask(
                        @Parameter(description = "Task ID.", example = "7d1f1f5e-8b8c-4c30-9c1c-9fd91a9e7a23") @PathVariable UUID id) {

                taskService.deleteTask(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

}
