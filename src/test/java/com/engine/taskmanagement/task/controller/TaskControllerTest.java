package com.engine.taskmanagement.task.controller;

import com.engine.taskmanagement.task.dto.request.ChangeTaskPriorityRequest;
import com.engine.taskmanagement.task.dto.request.ChangeTaskStatusRequest;
import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
import com.engine.taskmanagement.task.dto.request.PartialUpdateTaskRequest;
import com.engine.taskmanagement.task.dto.request.UpdateTaskRequest;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void createTaskReturnsCreatedTask() throws Exception {
        CreateTaskRequest request = createTaskRequest("Controller task");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Controller task"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTaskWithoutTitleReturnsBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setDescription("Missing title");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTasksReturnsOnlyActiveTasks() throws Exception {
        TaskResponse activeTask = createTask("Active task");
        TaskResponse deletedTask = createTask("Deleted task");
        mockMvc.perform(delete("/api/tasks/{id}", deletedTask.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(activeTask.getId()))
                .andExpect(jsonPath("$[0].title").value("Active task"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTaskByIdReturnsTask() throws Exception {
        TaskResponse task = createTask("Find me");

        mockMvc.perform(get("/api/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Find me"));
    }

    @Test
    void getTaskByMissingIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskReturnsUpdatedTask() throws Exception {
        TaskResponse task = createTask("Old title");
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New title");
        request.setDescription("New description");
        request.setPriority(TaskPriority.URGENT);
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setDueDate(LocalDate.now().plusDays(2));

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void partialUpdateTaskReturnsPatchedTask() throws Exception {
        TaskResponse task = createTask("Original title");
        PartialUpdateTaskRequest request = new PartialUpdateTaskRequest();
        request.setTitle("Patched title");
        request.setPriority(TaskPriority.HIGH);

        mockMvc.perform(patch("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patched title"))
                .andExpect(jsonPath("$.description").value("Original description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void deleteTaskSoftDeletesTaskAndDeletedListReturnsIt() throws Exception {
        TaskResponse task = createTask("Delete me");

        mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tasks/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(task.getId()))
                .andExpect(jsonPath("$[0].title").value("Delete me"));
    }

    @Test
    void restoreTaskReturnsTaskToActiveList() throws Exception {
        TaskResponse task = createTask("Restore me");
        mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/tasks/{id}/restore", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(task.getId()));
    }

    @Test
    void hardDeleteTaskRemovesTask() throws Exception {
        TaskResponse task = createTask("Hard delete me");

        mockMvc.perform(delete("/api/tasks/{id}/hard", task.getId()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void changeStatusReturnsTaskWithNewStatus() throws Exception {
        TaskResponse task = createTask("Change status");
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
        request.setStatus(TaskStatus.DONE);

        mockMvc.perform(post("/api/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void changePriorityReturnsTaskWithNewPriority() throws Exception {
        TaskResponse task = createTask("Change priority");
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(TaskPriority.URGENT);

        mockMvc.perform(post("/api/tasks/{id}/priority", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("URGENT"));
    }

    @Test
    void getTasksFiltersByStatus() throws Exception {
        TaskResponse todoTask = createTask("Todo task");
        TaskResponse doneTask = createTask("Done task");
        changeStatus(doneTask.getId(), TaskStatus.DONE);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(todoTask.getId()));
    }

    @Test
    void getTasksFiltersByPriority() throws Exception {
        TaskResponse mediumTask = createTask("Medium task");
        TaskResponse urgentTask = createTask("Urgent task");
        changePriority(urgentTask.getId(), TaskPriority.URGENT);

        mockMvc.perform(get("/api/tasks")
                        .param("priority", "URGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(urgentTask.getId()));

        mockMvc.perform(get("/api/tasks")
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(mediumTask.getId()));
    }

    @Test
    void getTasksFiltersByStatusAndPriority() throws Exception {
        TaskResponse todoHighTask = createTask("Todo high task");
        TaskResponse doneHighTask = createTask("Done high task");
        TaskResponse todoLowTask = createTask("Todo low task");
        changePriority(todoHighTask.getId(), TaskPriority.HIGH);
        changePriority(doneHighTask.getId(), TaskPriority.HIGH);
        changeStatus(doneHighTask.getId(), TaskStatus.DONE);
        changePriority(todoLowTask.getId(), TaskPriority.LOW);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "TODO")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(todoHighTask.getId()));
    }

    private TaskResponse createTask(String title) throws Exception {
        String content = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskRequest(title))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, TaskResponse.class);
    }

    private CreateTaskRequest createTaskRequest(String title) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(title);
        request.setDescription("Original description");
        return request;
    }

    private void changeStatus(Long taskId, TaskStatus taskStatus) throws Exception {
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
        request.setStatus(taskStatus);
        mockMvc.perform(post("/api/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void changePriority(Long taskId, TaskPriority priority) throws Exception {
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(priority);
        mockMvc.perform(post("/api/tasks/{id}/priority", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
