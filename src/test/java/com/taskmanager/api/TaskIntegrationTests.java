package com.taskmanager.api;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.UpdateTaskRequest;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;
import com.taskmanager.api.mapper.TaskMapper;
import com.taskmanager.api.repository.TaskRepository;

/*
    Integration testing using MockMvc loads the full Spring application context
    and tests the complete request flow through the controller, service,
    repository, and database without starting a real HTTP server.
*/
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TaskIntegrationTests {

        @Container
        static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
                        .withDatabaseName("taskmanager_test")
                        .withUsername("test")
                        .withPassword("test");

        @DynamicPropertySource
        static void setProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl);
                registry.add("spring.datasource.username", postgres::getUsername);
                registry.add("spring.datasource.password", postgres::getPassword);
        }

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private TaskRepository taskRepository;

        @Autowired
        private TaskMapper mapper;

        @BeforeEach
        public void setup() {
                taskRepository.deleteAll();
        }

        @Test
        public void createTask_shouldReturn201_whenRequestIsValid() throws Exception {

                CreateTaskRequest taskRequest = createValidTaskRequest();

                // Convert the DTO to a String
                String req = objectMapper.writeValueAsString(taskRequest);

                RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req);

                mockMvc.perform(request)
                                .andExpect(status().is2xxSuccessful())
                                .andExpect(jsonPath("$.title").value("Buy groceries"))
                                .andReturn();
        }

        @Test
        public void createTask_shouldReturn400_whenDueDateIsMissing() throws Exception {

                CreateTaskRequest taskRequest = new CreateTaskRequest();

                taskRequest.setTitle("My productive life");
                taskRequest.setDescription("I will travel Asia first, then explore Europe and Africa.");
                taskRequest.setStatus(TaskStatus.IN_PROGRESS);
                taskRequest.setPriority(TaskPriority.HIGH);
                taskRequest.setDueDate(null);

                // Convert the DTO to a String
                String req = objectMapper.writeValueAsString(taskRequest);

                RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void getTask_shouldReturn200_whenRequestIsValid() throws Exception {

                // We create the task
                CreateTaskRequest taskRequest = createValidTaskRequest();

                Task savedTask = taskRepository.save(mapper.toEntity(taskRequest));

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/{id}", savedTask.getId());

                mockMvc.perform(request)
                                .andExpect(status().is2xxSuccessful())
                                .andExpect(jsonPath("$.id").value(savedTask.getId().toString()))
                                .andExpect(jsonPath("$.title").value("Buy groceries"))
                                .andExpect(jsonPath("$.description").value("I will buy rice, bread, and avocados"))
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                                .andExpect(jsonPath("$.priority").value("HIGH"))
                                .andExpect(jsonPath("$.dueDate").value(LocalDate.now().plusDays(2).toString()));
                ;

        }

        @Test
        public void getTask_shouldReturn404_whenTaskDoesNotExist() throws Exception {

                UUID id = UUID.randomUUID();

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/{id}", id);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());

        }

        @Test
        public void updateTask_shouldReturn200_whenRequestIsValid() throws Exception {

                // We create the task
                CreateTaskRequest taskRequest = createValidTaskRequest();

                Task savedTask = taskRepository.save(mapper.toEntity(taskRequest));

                UpdateTaskRequest update = new UpdateTaskRequest();
                update.setTitle("Buy groceries");
                update.setDescription("I will buy rice, bread, avocados, juice, and milk");
                update.setStatus(TaskStatus.DONE);
                update.setPriority(TaskPriority.MEDIUM);
                update.setDueDate(LocalDate.now().plusDays(5));

                // Convert the DTO to a String
                String req = objectMapper.writeValueAsString(update);

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", savedTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req);

                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(savedTask.getId().toString()))
                                .andExpect(jsonPath("$.title").value("Buy groceries"))
                                .andExpect(jsonPath("$.description")
                                                .value("I will buy rice, bread, avocados, juice, and milk"))
                                .andExpect(jsonPath("$.status").value("DONE"))
                                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                                .andExpect(jsonPath("$.dueDate").value(LocalDate.now().plusDays(5).toString()));
        }

        @Test
        public void updateTask_shouldReturn404_whenTaskDoesNotExist() throws Exception {

                UUID id = UUID.randomUUID();

                UpdateTaskRequest update = new UpdateTaskRequest();
                update.setTitle("Buy groceries");
                update.setDescription("I will buy rice, bread, avocados, juice, and milk");
                update.setStatus(TaskStatus.DONE);
                update.setPriority(TaskPriority.MEDIUM);
                update.setDueDate(LocalDate.now().plusDays(5));

                // Convert the DTO to a String
                String req = objectMapper.writeValueAsString(update);

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void deleteTask_shouldReturn204_whenRequestIsValid() throws Exception {

                // We create the task
                CreateTaskRequest taskRequest = createValidTaskRequest();

                Task savedTask = taskRepository.save(mapper.toEntity(taskRequest));

                RequestBuilder request = MockMvcRequestBuilders.delete("/api/v1/tasks/{id}", savedTask.getId());

                mockMvc.perform(request)
                                .andExpect(status().isNoContent());

                RequestBuilder getRequest = MockMvcRequestBuilders.get("/api/v1/tasks/{id}", savedTask.getId());

                mockMvc.perform(getRequest)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void deleteTask_shouldReturn404_whenTaskDoesNotExist() throws Exception {

                UUID id = UUID.randomUUID();

                RequestBuilder request = MockMvcRequestBuilders.delete("/api/v1/tasks/{id}", id);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void listTasks_shouldReturn200_whenRequestIsValid() throws Exception {

                // We create the tasks
                CreateTaskRequest taskRequest = createValidTaskRequest();

                Task task1 = taskRepository.save(mapper.toEntity(taskRequest));

                Task task2 = mapper.toEntity(taskRequest);
                task2.setTitle("Workout");
                task2.setDescription("Play basketball for at least one hour");
                task2.setStatus(TaskStatus.TODO);
                task2.setPriority(TaskPriority.MEDIUM);
                task2.setDueDate(LocalDate.now().plusDays(4));

                taskRepository.save(task2);

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "title,asc");

                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(2))
                                .andExpect(jsonPath("$.content[0].id").value(task1.getId().toString()))
                                .andExpect(jsonPath("$.content[0].title").value("Buy groceries"))
                                .andExpect(jsonPath("$.content[1].title").value("Workout"));
        }

        @Test
        public void listTasks_shouldReturn200_whenRequestHasFilters() throws Exception {

                // We create the tasks
                CreateTaskRequest taskRequest = createValidTaskRequest();

                taskRepository.save(mapper.toEntity(taskRequest));

                Task task2 = mapper.toEntity(taskRequest);
                task2.setTitle("Workout");
                task2.setDescription("Play basketball for at least one hour");
                task2.setStatus(TaskStatus.TODO);
                task2.setPriority(TaskPriority.MEDIUM);
                task2.setDueDate(LocalDate.now().plusDays(4));

                taskRepository.save(task2);

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("q", "basketball")
                                .param("status", "TODO")
                                .param("priority", "MEDIUM")
                                .param("dueBefore", LocalDate.now().plusDays(5).toString());

                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(1))
                                .andExpect(jsonPath("$.content[0].title").value("Workout"))
                                .andExpect(jsonPath("$.content[0].description")
                                                .value("Play basketball for at least one hour"))
                                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                                .andExpect(jsonPath("$.content[0].priority").value("MEDIUM"))
                                .andExpect(jsonPath("$.content[0].dueDate")
                                                .value(LocalDate.now().plusDays(4).toString()));
        }

        @Test
        public void updateTask_shouldReturn400_whenTitleIsBlank() throws Exception {

                // We create the task
                CreateTaskRequest taskRequest = createValidTaskRequest();

                Task savedTask = taskRepository.save(mapper.toEntity(taskRequest));

                String json = """
                                {
                                  "title": "",
                                  "description": "I will play basketball for 2 hours",
                                  "status": "TODO",
                                  "priority": "HIGH",
                                  "dueDate": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(3));

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", savedTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void updateTask_shouldReturn400_whenStatusIsMissing() throws Exception {

                // We create the task
                CreateTaskRequest taskRequest = createValidTaskRequest();

                Task savedTask = taskRepository.save(mapper.toEntity(taskRequest));

                String json = """
                                {
                                  "title": "Buy groceries",
                                  "description": "I will play basketball for 2 hours",
                                  "priority": "HIGH",
                                  "dueDate": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(3));

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", savedTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void updateTask_shouldReturn400_whenDueDateIsInPast() throws Exception {

                // We create the task
                CreateTaskRequest taskRequest = createValidTaskRequest();

                Task savedTask = taskRepository.save(mapper.toEntity(taskRequest));

                UpdateTaskRequest update = new UpdateTaskRequest();
                update.setTitle("Buy groceries");
                update.setDescription("I will buy rice, bread, avocados, juice, and milk");
                update.setStatus(TaskStatus.DONE);
                update.setPriority(TaskPriority.MEDIUM);
                update.setDueDate(LocalDate.now().minusDays(1));

                // Convert the DTO to a String
                String req = objectMapper.writeValueAsString(update);

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", savedTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void updateTask_shouldReturn400_whenStatusIsInvalid() throws Exception {

                UUID id = UUID.randomUUID();

                String json = """
                                {
                                  "title": "Buy groceries",
                                  "description": "I will play basketball for 2 hours",
                                  "status": "INVALID",
                                  "priority": "HIGH",
                                  "dueDate": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(3));

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void updateTask_shouldReturn400_whenPriorityIsInvalid() throws Exception {

                UUID id = UUID.randomUUID();

                String json = """
                                {
                                  "title": "Buy groceries",
                                  "description": "I will play basketball for 2 hours",
                                  "status": "TODO",
                                  "priority": "INVALID",
                                  "dueDate": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(3));

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void listTasks_shouldReturn400_whenStatusIsInvalid() throws Exception {

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("status", "INVALID");

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void listTasks_shouldReturn400_whenPriorityIsInvalid() throws Exception {

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("priority", "INVALID");

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        private CreateTaskRequest createValidTaskRequest() {

                CreateTaskRequest taskRequest = new CreateTaskRequest();

                taskRequest.setTitle("Buy groceries");
                taskRequest.setDescription("I will buy rice, bread, and avocados");
                taskRequest.setStatus(TaskStatus.IN_PROGRESS);
                taskRequest.setPriority(TaskPriority.HIGH);
                taskRequest.setDueDate(LocalDate.now().plusDays(2));

                return taskRequest;
        }
}
