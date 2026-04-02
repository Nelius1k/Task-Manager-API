package com.taskmanager.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.checkerframework.checker.units.qual.t;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.TaskResponse;
import com.taskmanager.api.dto.TaskStatusPatch;
import com.taskmanager.api.dto.UpdateTaskRequest;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;
import com.taskmanager.api.exception.InvalidTaskException;
import com.taskmanager.api.exception.TaskNotFoundException;
import com.taskmanager.api.mapper.TaskMapper;
import com.taskmanager.api.repository.TaskRepository;
import com.taskmanager.api.service.TaskServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTests {

    // Mock the dependency (repository) so no real database is used
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper mapper;

    // We use @InjectMocks for the actual class we are testing
    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    public void createTask_shouldReturnTask_whenRequestIsValid() {

        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setTitle("My productive life");
        taskRequest.setDescription("I will travel Asia first, then explore Europe and Africa.");
        taskRequest.setStatus(TaskStatus.TODO);
        taskRequest.setPriority(TaskPriority.HIGH);
        taskRequest.setDueDate(LocalDate.now().plusDays(5));

        Task task = defaultTask();

        // Stub mapper: convert request DTO → entity
        // Important: mocks return null by default, so we must define this behavior
        given(mapper.toEntity(any(CreateTaskRequest.class))).willReturn(task);

        // Stub repository: simulate saving the entity and returning the persisted
        // object
        // Use any(Task.class) because the service creates a NEW Task instance (not the
        // same object)
        given(taskRepository.save(any(Task.class))).willReturn(task);

        // Expected response DTO after mapping entity → response
        // This represents what the service should return
        TaskResponse mockedResponse = new TaskResponse();
        mockedResponse.setTitle("My productive life");
        mockedResponse.setDescription("I will travel Asia first, then explore Europe and Africa.");
        mockedResponse.setStatus(TaskStatus.TODO);
        mockedResponse.setPriority(TaskPriority.HIGH);
        mockedResponse.setDueDate(LocalDate.now().plusDays(5));

        // Stub mapper: convert entity → response DTO
        // Important: willReturn(...) MUST use a real object (not any(...))
        given(mapper.toResponse(any(Task.class))).willReturn(mockedResponse);

        // Act: call the real service method (this is what we are testing)
        TaskResponse response = taskService.createTask(taskRequest);

        // Verify the response is not null and fields are correctly mapped
        assertNotNull(response);
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getStatus(), response.getStatus());
        assertEquals(task.getPriority(), response.getPriority());
        assertEquals(task.getDueDate(), response.getDueDate());

        verify(taskRepository, times(1)).save(any(Task.class));

    }

    @Test
    public void createTask_shouldThrowException_whenDueDateIsInPast() {

        // Arrange: build a request was the due date is in the past relative to the
        // current day
        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setTitle("Buy groceries");
        taskRequest.setDescription("Milk, eggs, bread");
        taskRequest.setStatus(TaskStatus.TODO);
        taskRequest.setPriority(TaskPriority.MEDIUM);
        taskRequest.setDueDate(LocalDate.now().minusDays(1));

        InvalidTaskException ex = assertThrows(InvalidTaskException.class, () -> {
            taskService.createTask(taskRequest);
        });

        assertEquals("Due date cannot be in the past", ex.getMessage());

        verify(mapper, never()).toEntity(any());
        verify(mapper, never()).toResponse(any());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void createTask_shouldDefaultPriorityToMedium_whenPriorityIsNull() {

        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setTitle("My productive life");
        taskRequest.setDescription("I will travel Asia first, then explore Europe and Africa.");
        taskRequest.setStatus(TaskStatus.TODO);
        taskRequest.setPriority(null);
        taskRequest.setDueDate(LocalDate.now().plusDays(5));

        Task task = new Task();
        task.setTitle("My productive life");
        task.setDescription("I will travel Asia first, then explore Europe and Africa.");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setDueDate(LocalDate.now().plusDays(5));

        given(mapper.toEntity(any(CreateTaskRequest.class))).willReturn(task);

        given(taskRepository.save(any(Task.class))).willReturn(task);

        TaskResponse mockedResponse = new TaskResponse();
        mockedResponse.setTitle("My productive life");
        mockedResponse.setDescription("I will travel Asia first, then explore Europe and Africa.");
        mockedResponse.setStatus(TaskStatus.TODO);
        mockedResponse.setPriority(TaskPriority.MEDIUM);
        mockedResponse.setDueDate(LocalDate.now().plusDays(5));

        given(mapper.toResponse(any(Task.class))).willReturn(mockedResponse);

        TaskResponse response = taskService.createTask(taskRequest);

        assertEquals(TaskPriority.MEDIUM, taskRequest.getPriority());

        assertNotNull(response);
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getStatus(), response.getStatus());
        assertEquals(task.getPriority(), response.getPriority());
        assertEquals(task.getDueDate(), response.getDueDate());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void createTask_shouldDefaultStatusToTodo_whenStatusIsNull() {

        // Arrange: build a request where status is null (service should default it)
        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setTitle("Finish API documentation");
        taskRequest.setDescription("Complete Swagger docs for Task Manager API");
        taskRequest.setStatus(null);
        taskRequest.setPriority(TaskPriority.HIGH);
        taskRequest.setDueDate(LocalDate.now().plusDays(5));

        // Arrange: mock entity that will be returned after mapping and saving
        // status is TODO because that's what we expect after defaulting
        Task task = new Task();
        task.setTitle("Finish API documentation");
        task.setDescription("Complete Swagger docs for Task Manager API");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(5));

        // Stub: mapper converts request to entity
        // We just want to make sure mapping happens
        given(mapper.toEntity(any(CreateTaskRequest.class))).willReturn(task);

        // Stub: repository simulates saving and returning the persisted entity
        given(taskRepository.save(any(Task.class))).willReturn(task);

        // Arrange: this is the expected response DTO after mapping entity to response
        TaskResponse mockedResponse = new TaskResponse();
        mockedResponse.setTitle("Finish API documentation");
        mockedResponse.setDescription("Complete Swagger docs for Task Manager API");
        mockedResponse.setStatus(TaskStatus.TODO);
        mockedResponse.setPriority(TaskPriority.HIGH);
        mockedResponse.setDueDate(LocalDate.now().plusDays(5));

        // Stub: mapper converts entity to the response DTO
        given(mapper.toResponse(any(Task.class))).willReturn(mockedResponse);

        // Act: call the service method (this is what we're testing)
        TaskResponse response = taskService.createTask(taskRequest);

        // Assert: service should have defaulted status from null to TODO
        // This proves the business logic inside the service ran
        assertEquals(TaskStatus.TODO, taskRequest.getStatus());

        // Assert: response is returned and correctly mapped
        assertNotNull(response);
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getStatus(), response.getStatus());
        assertEquals(task.getPriority(), response.getPriority());
        assertEquals(task.getDueDate(), response.getDueDate());

        // Verify: check that entity was saved exactly once
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void getTask_shouldReturnTask_whenRequestIsValid() {

        UUID id = UUID.randomUUID();
        Task task = new Task();
        task.setId(id);
        task.setTitle("Finish API documentation");
        task.setDescription("Complete Swagger docs for Task Manager API");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(5));

        given(taskRepository.findById(any(UUID.class))).willReturn(Optional.of(task));

        TaskResponse mockedResponse = new TaskResponse();
        mockedResponse.setId(id);
        mockedResponse.setTitle("Finish API documentation");
        mockedResponse.setDescription("Complete Swagger docs for Task Manager API");
        mockedResponse.setStatus(TaskStatus.TODO);
        mockedResponse.setPriority(TaskPriority.HIGH);
        mockedResponse.setDueDate(LocalDate.now().plusDays(5));

        given(mapper.toResponse(any(Task.class))).willReturn(mockedResponse);

        // Act: call the real service method (this is what we are testing)
        TaskResponse response = taskService.getTask(id);

        assertNotNull(response);
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getStatus(), response.getStatus());
        assertEquals(task.getPriority(), response.getPriority());
        assertEquals(task.getDueDate(), response.getDueDate());

        verify(taskRepository, times(1)).findById(id);

    }

    @Test
    public void getTask_shouldThrowTaskNotFoundException_whenIdDoesNotExist() {

        // generate an ID that does not exist in the database
        UUID id2 = UUID.randomUUID();

        // simulate the repository returning no result for this ID
        given(taskRepository.findById(id2)).willReturn(Optional.empty());

        // attempt to fetch task and expect a not found exception
        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTask(id2);
        });

        // verify the exception message contains the correct ID
        assertEquals("The task " + id2 + " cannot be found", ex.getMessage());

        // ensure mapping is never reached since no task was found
        verify(mapper, never()).toResponse(any());

        // confirm the repository was called exactly once with the given ID
        verify(taskRepository, times(1)).findById(id2);
    }

    @Test
    public void listTasks_shouldReturnTasks_whenRequestIsValid() {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Task task1 = new Task();
        task1.setId(id1);
        task1.setTitle("Buy groceries");
        task1.setDescription("Buy rice, bread, and avocados");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task1.setPriority(TaskPriority.HIGH);
        task1.setDueDate(LocalDate.now().plusDays(2));
        task1.setCreatedAt(Instant.now());
        task1.setUpdatedAt(Instant.now());

        Task task2 = new Task();
        task2.setId(id2);
        task2.setTitle("Workout");
        task2.setDescription("Play basketball for at least one hour");
        task2.setStatus(TaskStatus.TODO);
        task2.setPriority(TaskPriority.MEDIUM);
        task2.setDueDate(LocalDate.now().plusDays(4));
        task2.setCreatedAt(Instant.now());
        task2.setUpdatedAt(Instant.now());

        List<Task> tasks = new ArrayList<>();

        tasks.add(task1);
        tasks.add(task2);

        // Turn the list into a page because the respository returns a page
        Page<Task> pages = new PageImpl<>(tasks, PageRequest.of(0, 10), tasks.size());

        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").descending());

        // Mock the repository method
        given(taskRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(pages);

        // Act: call the real service method (this is what we are testing)
        taskService.listTasks(null, null, null, null, pageable);

        verify(mapper, times(1)).toResponse(task1);
        verify(mapper, times(1)).toResponse(task2);
    }

    @Test
    public void updateTask_shoulsReturnUpdatedTask_whenRequestIsValid() {

        UUID id = UUID.randomUUID();
        UpdateTaskRequest taskRequest = new UpdateTaskRequest();
        taskRequest.setTitle("Watch a movie");
        taskRequest.setDescription("Relax and watch something on Netflix");
        taskRequest.setStatus(TaskStatus.TODO);
        taskRequest.setPriority(TaskPriority.HIGH);
        taskRequest.setDueDate(LocalDate.now().plusDays(2));

        Task task = new Task();
        task.setId(id);
        task.setTitle("Watch a movie");
        task.setDescription("Relax and watch something on Netflix");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(2));

        TaskResponse response = new TaskResponse();
        response.setId(id);
        response.setTitle("Watch a movie");
        response.setDescription("Relax and watch something on Netflix");
        response.setStatus(TaskStatus.TODO);
        response.setPriority(TaskPriority.HIGH);
        response.setDueDate(LocalDate.now().plusDays(2));

        given(taskRepository.findById(id)).willReturn(Optional.of(task));

        given(taskRepository.save(any(Task.class))).willReturn(task);

        given(mapper.toResponse(task)).willReturn(response);

        TaskResponse updated = taskService.updateTask(id, taskRequest);

        assertEquals(response, updated);
        assertEquals(response.getTitle(), updated.getTitle());
        assertEquals(response.getDescription(), updated.getDescription());
        assertEquals(response.getStatus(), updated.getStatus());
        assertEquals(response.getPriority(), updated.getPriority());

        verify(taskRepository).save(any(Task.class));
        verify(mapper).toResponse(task);
    }

    @Test
    public void updateTask_shouldThrowException_whenDueDateIsInPast() {

        UUID id = UUID.randomUUID();
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle(defaultTask().getTitle());
        request.setDescription(defaultTask().getDescription());
        request.setStatus(defaultTask().getStatus());
        request.setPriority(defaultTask().getPriority());
        request.setDueDate(LocalDate.now().minusDays(2));

        InvalidTaskException ex = assertThrows(InvalidTaskException.class, () -> {
            taskService.updateTask(id, request);
        });

        assertEquals("Due date cannot be in the past", ex.getMessage());

        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());

    }

    @Test
    public void updateTask_shouldThrowTaskNotFoundException_whenTaskDoesNotExist() {

        Task defaultTask = defaultTask();

        UUID id = UUID.randomUUID();

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle(defaultTask.getTitle());
        request.setDescription(defaultTask.getDescription());
        request.setStatus(defaultTask.getStatus());
        request.setPriority(defaultTask.getPriority());
        request.setDueDate(defaultTask.getDueDate());

        given(taskRepository.findById(id)).willReturn(Optional.empty());

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(id, request);
        });

        assertEquals("The task " + id + " cannot be found", ex.getMessage());

        verify(taskRepository).findById(id);
        verify(taskRepository, never()).save(any());
        verify(mapper, never()).toResponse(any());

    }

    @Test
    public void updateTaskStatus_shouldReturnUpdatedTask_whenRequestIsValid() {

        Task task = defaultTask();

        UUID id = UUID.randomUUID();

        TaskStatusPatch update = new TaskStatusPatch();
        update.setStatus(TaskStatus.DONE);

        task.setStatus(update.getStatus());

        TaskResponse response = new TaskResponse();
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(update.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());

        given(taskRepository.findById(id)).willReturn(Optional.of(task));

        given(taskRepository.save(any(Task.class))).willReturn(task);

        given(mapper.toResponse(any(Task.class))).willReturn(response);

        TaskResponse finalResponse = taskService.updateTaskStatus(id, update);

        assertEquals(response.getStatus(), finalResponse.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void deleteTask_shouldThrowTaskNotFoundException_whenTaskDoesNotExist() {

        UUID id = UUID.randomUUID();

        given(taskRepository.findById(id)).willReturn(Optional.empty());

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(id);
        });

        assertEquals("The task " + id + " cannot be found", ex.getMessage());

        verify(taskRepository).findById(id);
        verify(taskRepository, never()).deleteById(id);

    }

    private Task defaultTask() {

        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("My productive life");
        task.setDescription("I will travel Asia first, then explore Europe and Africa.");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(5));

        return task;
    }

}
