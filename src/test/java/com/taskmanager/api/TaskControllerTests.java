package com.taskmanager.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.api.controller.TaskController;
import com.taskmanager.api.dto.CreateTaskRequest;
import com.taskmanager.api.dto.TaskResponse;
import com.taskmanager.api.dto.TaskStatusPatch;
import com.taskmanager.api.dto.UpdateTaskRequest;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;
import com.taskmanager.api.service.TaskService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc
class TaskControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TaskService taskService;

        /*
         * Test methods naming convention:
         * methodUnderTest_shouldExpectedBehavior_whenCondition
         */

        @Test
        public void createTask_shouldReturn201_whenRequestIsValid() throws Exception {

                CreateTaskRequest taskRequest = new CreateTaskRequest();

                taskRequest.setTitle("My productive life");
                taskRequest.setDescription("I will travel Asia first, then explore Europe and Africa.");
                taskRequest.setStatus(TaskStatus.IN_PROGRESS);
                taskRequest.setPriority(TaskPriority.HIGH);
                taskRequest.setDueDate(LocalDate.of(2026, 03, 15));

                // Convert the DTO to a String
                String req = objectMapper.writeValueAsString(taskRequest);

                RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req);

                mockMvc.perform(request)
                                .andExpect(status().is2xxSuccessful());
        }

        @Test
        public void createTask_shouldReturn400_whenTitleIsBlank() throws Exception {

                CreateTaskRequest taskRequest = new CreateTaskRequest();

                taskRequest.setTitle("");
                taskRequest.setDescription("I will play basketball for 2 hours");
                taskRequest.setStatus(TaskStatus.TODO);
                taskRequest.setPriority(TaskPriority.HIGH);
                taskRequest.setDueDate(LocalDate.of(2026, 03, 15));

                // Convert the DTO to a String
                String req = objectMapper.writeValueAsString(taskRequest);

                RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void createTask_shouldReturn400_whenStatusIsInvalid() throws Exception {

                String json = """
                                {
                                  "title": "My day",
                                  "description": "I will play fortnite, then I will make food",
                                  "status": "INVALID",
                                  "priority": "HIGH",
                                  "dueDate": "2026-03-15"
                                }
                                """;
                RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void createTask_shouldReturn400_whenRequestBodyIsMalformed() throws Exception {

                // There is a missing comma in the JSON body for testing
                String json = """
                                {
                                  "title": "My day",
                                  "description": "I will play fortnite, then I will make food",
                                  "status": "TODO"
                                  "priority": "HIGH",
                                  "dueDate": "2026-03-15"
                                }
                                """;
                RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void getTask_shouldReturn200_whenRequestIsValid() throws Exception {

                UUID id = UUID.randomUUID();

                TaskResponse task = new TaskResponse();
                task.setId(id);
                task.setTitle("Buy groceries");
                task.setDescription("I will buy rice, bread, and avocados");
                task.setStatus(TaskStatus.TODO);
                task.setPriority(TaskPriority.HIGH);
                task.setDueDate(LocalDate.of(2026, 03, 15));

                // mock the service
                when(taskService.getTask(id)).thenReturn(task);

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/{id}", id);

                // mock the request and assert the correctness of the fields
                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(id.toString()))
                                .andExpect(jsonPath("$.title").value("Buy groceries"))
                                .andExpect(jsonPath("$.description").value("I will buy rice, bread, and avocados"))
                                .andExpect(jsonPath("$.status").value("TODO"))
                                .andExpect(jsonPath("$.priority").value("HIGH"))
                                .andExpect(jsonPath("$.dueDate").value("2026-03-15"));
        }

        @Test
        public void listTasks_shouldReturn200_whenRequestIsValid() throws Exception {

                UUID id1 = UUID.randomUUID();
                UUID id2 = UUID.randomUUID();

                TaskResponse task1 = new TaskResponse();
                task1.setId(id1);
                task1.setTitle("Buy groceries");
                task1.setDescription("Buy rice, bread, and avocados");
                task1.setStatus(TaskStatus.TODO);
                task1.setPriority(TaskPriority.HIGH);
                task1.setDueDate(LocalDate.now().plusDays(2));
                task1.setCreatedAt(Instant.now());
                task1.setUpdatedAt(Instant.now());

                TaskResponse task2 = new TaskResponse();
                task2.setId(id2);
                task2.setTitle("Workout");
                task2.setDescription("Play basketball for at least one hour");
                task2.setStatus(TaskStatus.TODO);
                task2.setPriority(TaskPriority.MEDIUM);
                task2.setDueDate(LocalDate.now().plusDays(4));
                task2.setCreatedAt(Instant.now());
                task2.setUpdatedAt(Instant.now());

                List<TaskResponse> tasks = new ArrayList<>();

                tasks.add(task1);
                tasks.add(task2);

                // We convert the list to a page because the Service methode returns a
                // Page<TaskResponse>.
                Page<TaskResponse> pages = new PageImpl<>(tasks, PageRequest.of(0, 10), tasks.size());

                // We mock the service listTasks method
                when(taskService.listTasks(null, TaskStatus.TODO, null, null, PageRequest.of(0, 10)))
                                .thenReturn(pages);

                // Build the HTTP request that will be sent to the controller
                // This simulates a client calling the search endpoint with query parameters
                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("status", "TODO");

                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(2)) // validate the number of tasks
                                // returned
                                .andExpect(jsonPath("$.content[0].title").value(
                                                "Buy groceries")) // validate the value of the title of the first task
                                // in the response
                                .andExpect(jsonPath("$.content[1].title").value(
                                                "Workout"));
        }

        @Test
        public void listTasks_shouldReturn400_whenStatusIsInvalid() throws Exception {

                // Build the HTTP request that will be sent to the controller
                // This simulates a client calling the search endpoint with query parameters
                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("status", "INVALID");

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void listTasks_shouldReturn400_whenPriorityIsInvalid() throws Exception {

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("priority", "INVALID");

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void listTasks_shouldReturn400_whenDueBeforeDateIsInvalid() throws Exception {

                RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/tasks/search")
                                .param("page", "0")
                                .param("size", "10")
                                .param("dueBefore", "2026-13-01");

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void updateTask_shouldReturn200_whenRequestIsValid() throws Exception {

                UUID id = UUID.randomUUID();

                UpdateTaskRequest task = new UpdateTaskRequest();
                task.setTitle("Buy groceries");
                task.setDescription("I will buy rice, bread, and avocados");
                task.setStatus(TaskStatus.TODO);
                task.setPriority(TaskPriority.HIGH);
                task.setDueDate(LocalDate.of(2026, 03, 15));

                // Convert the request DTO into JSON because MockMvc sends HTTP requests as JSON
                String taskRequest = objectMapper.writeValueAsString(task);

                // Build the response DTO that the mocked service will return
                // This simulates the updated task returned by the service layer
                TaskResponse updatedTask = new TaskResponse();
                updatedTask.setTitle("Buy groceries");
                updatedTask.setDescription("I will buy rice, bread, avocados, juice, and milk");
                updatedTask.setStatus(TaskStatus.IN_PROGRESS);
                updatedTask.setPriority(TaskPriority.HIGH);
                updatedTask.setDueDate(LocalDate.of(2026, 03, 15));

                // Mock the service call so that when the controller calls updateTask(...)
                // the mocked service returns the updated task instead of executing real logic
                when(taskService.updateTask(eq(id), any(UpdateTaskRequest.class))).thenReturn(updatedTask);

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(taskRequest);

                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Buy groceries"))
                                .andExpect(jsonPath("$.description")
                                                .value("I will buy rice, bread, avocados, juice, and milk"))
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        public void updateTask_shouldReturn400_whenTitleIsBlank() throws Exception {

                UUID id = UUID.randomUUID();
                String json = """
                                {
                                  "title": "",
                                  "description": "I will play fortnite, then I will make food",
                                  "status": "TODO",
                                  "priority": "HIGH",
                                  "dueDate": "2026-03-15"
                                }
                                """;

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void updateTask_shouldReturn400_whenStatusIsInvalid() throws Exception {

                UUID id = UUID.randomUUID();
                String json = """
                                {
                                  "title": "Buy groceries",
                                  "description": "I will play fortnite, then I will make food",
                                  "status": "INVALID",
                                  "priority": "HIGH",
                                  "dueDate": "2026-03-15"
                                }
                                """;

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void updateTask_shouldReturn400_whenPriorityIsInvalid() throws Exception {

                UUID id = UUID.randomUUID();
                String json = """
                                {
                                  "title": "Buy groceries",
                                  "description": "I will play fortnite, then I will make food",
                                  "status": "TODO",
                                  "priority": "INVALID",
                                  "dueDate": "2026-03-15"
                                }
                                """;

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void updateTask_shouldReturn400_whenDueDateHasInvalidFormat() throws Exception {

                UUID id = UUID.randomUUID();
                String json = """
                                {
                                  "title": "Buy groceries",
                                  "description": "I will play fortnite, then I will make food",
                                  "status": "TODO",
                                  "priority": "LOW",
                                  "dueDate": "2026-13-15"
                                }
                                """;

                RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());

                /*
                 * We check to make sure the Service layer is never called since the request
                 * fails before the controller method finishes processing the request
                 */
                verifyNoInteractions(taskService);
        }

        @Test
        public void updateTaskStatus_shouldReturn200_whenRequestIsValid() throws Exception {

                TaskStatusPatch status = new TaskStatusPatch();
                status.setStatus(TaskStatus.IN_PROGRESS);

                String taskRequest = objectMapper.writeValueAsString(status);

                UUID id = UUID.randomUUID();
                TaskResponse updatedTask = new TaskResponse();
                updatedTask.setId(id);
                updatedTask.setTitle("Buy groceries");
                updatedTask.setDescription("I will buy rice, bread, and avocados");
                updatedTask.setStatus(TaskStatus.IN_PROGRESS);
                updatedTask.setPriority(TaskPriority.HIGH);
                updatedTask.setDueDate(LocalDate.of(2026, 03, 15));

                when(taskService.updateTaskStatus(eq(id), any(TaskStatusPatch.class))).thenReturn(updatedTask);

                RequestBuilder request = MockMvcRequestBuilders.patch("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(taskRequest);

                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        }

        @Test
        public void updateTaskStatus_shouldReturn400_whenStatusIsInvalid() throws Exception {

                UUID id = UUID.randomUUID();
                String json = """
                                {
                                  "status": "INVALID",
                                }
                                """;

                RequestBuilder request = MockMvcRequestBuilders.patch("/api/v1/tasks/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void deleteTask_shouldReturn204_whenRequestIsValid() throws Exception {

                UUID id = UUID.randomUUID();

                RequestBuilder request = MockMvcRequestBuilders.delete("/api/v1/tasks/{id}", id);

                mockMvc.perform(request)
                                .andExpect(status().isNoContent());

                verify(taskService, times(1)).deleteTask(id);
        }

        @Test
        public void deleteTask_shouldReturn400_whenIdIsInvalid() throws Exception {

                String id = "b4af1d36-8466-4474-8317";

                RequestBuilder request = MockMvcRequestBuilders.delete("/api/v1/tasks/{id}", id);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

}
