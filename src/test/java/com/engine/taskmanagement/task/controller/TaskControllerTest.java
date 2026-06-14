package com.engine.taskmanagement.task.controller;

import com.engine.taskmanagement.task.dto.request.ChangeTaskPriorityRequest;
import com.engine.taskmanagement.task.dto.request.ChangeTaskStatusRequest;
import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
import com.engine.taskmanagement.task.dto.request.PartialUpdateTaskRequest;
import com.engine.taskmanagement.task.dto.request.TaskTransitionRequest;
import com.engine.taskmanagement.task.dto.request.UpdateTaskRequest;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.activity.enums.TaskActivityType;
import com.engine.taskmanagement.task.criteria.dto.request.BulkCreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.CreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.UpdateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.response.AcceptanceCriteriaResponse;
import com.engine.taskmanagement.task.enums.Severity;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.enums.TaskType;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.auth.service.JwtService;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
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

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getTasksWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTaskReturnsCreatedTask() throws Exception {
        CreateTaskRequest request = createTaskRequest("Controller task");
        request.setTechnicalContext("Spring Security JWT authentication module");
        request.setExpectedOutcome("Old refresh tokens become invalid after successful refresh.");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Controller task"))
                .andExpect(jsonPath("$.type").value("FEATURE"))
                .andExpect(jsonPath("$.technicalContext").value("Spring Security JWT authentication module"))
                .andExpect(jsonPath("$.expectedOutcome").value("Old refresh tokens become invalid after successful refresh."))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTaskWithoutTitleReturnsBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setDescription("Missing title");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTaskWithoutTypeReturnsBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Missing type");
        request.setDescription("Type is required");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.type").value("Task type is required"));
    }

    @Test
    void createTaskWithInvalidTypeReturnsBadRequest() throws Exception {
        String request = """
                {
                  "title": "Invalid type",
                  "description": "Unsupported enum value",
                  "type": "OPERATIONS"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request value"));
    }

    @Test
    void createIncidentTaskWithoutSeverityReturnsBadRequest() throws Exception {
        CreateTaskRequest request = createTaskRequest("Investigate outage");
        request.setType(TaskType.INCIDENT);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Severity is required for incident and reliability tasks"));
    }

    @Test
    void createReliabilityTaskWithoutSeverityReturnsBadRequest() throws Exception {
        CreateTaskRequest request = createTaskRequest("Fix duplicate order creation");
        request.setType(TaskType.RELIABILITY);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Severity is required for incident and reliability tasks"));
    }

    @Test
    void getAllTasksReturnsOnlyActiveTasks() throws Exception {
        TaskResponse activeTask = createTask("Active task");
        TaskResponse deletedTask = createTask("Deleted task");
        mockMvc.perform(delete("/api/tasks/{id}", deletedTask.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(activeTask.getId()))
                .andExpect(jsonPath("$.content[0].title").value("Active task"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getTaskByIdReturnsTask() throws Exception {
        TaskResponse task = createTask("Find me");

        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Find me"));
    }

    @Test
    void getTaskByMissingIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999L)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskReturnsUpdatedTask() throws Exception {
        TaskResponse task = createTask("Old title");
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New title");
        request.setDescription("New description");
        request.setType(TaskType.REFACTOR);
        request.setSeverity(Severity.LOW);
        request.setTechnicalContext("Task controller update flow");
        request.setExpectedOutcome("Task details reflect the new technical context");
        request.setPriority(TaskPriority.URGENT);
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setDueDate(LocalDate.now().plusDays(2));

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.type").value("REFACTOR"))
                .andExpect(jsonPath("$.severity").value("LOW"))
                .andExpect(jsonPath("$.technicalContext").value("Task controller update flow"))
                .andExpect(jsonPath("$.expectedOutcome").value("Task details reflect the new technical context"))
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
                        .header("Authorization", "Bearer " + userToken())
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

        mockMvc.perform(delete("/api/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tasks/deleted")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(task.getId()))
                .andExpect(jsonPath("$.content[0].title").value("Delete me"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void restoreTaskReturnsTaskToActiveList() throws Exception {
        TaskResponse task = createTask("Restore me");
        mockMvc.perform(delete("/api/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/tasks/{id}/restore", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(task.getId()));
    }

    @Test
    void hardDeleteTaskRemovesTask() throws Exception {
        TaskResponse task = createTask("Hard delete me");

        mockMvc.perform(delete("/api/tasks/{id}/hard", task.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void hardDeleteTaskWithoutAdminReturnsUnauthorized() throws Exception {
        TaskResponse task = createTask("Hard delete me");

        mockMvc.perform(delete("/api/tasks/{id}/hard", task.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTaskWithAssigneeReturnsAssigneeId() throws Exception {
        User assignee = createUser("assignee@example.com");
        CreateTaskRequest request = createTaskRequest("Assigned task");
        request.setAssigneeId(assignee.getId());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assigneeId").value(assignee.getId()));
    }

    @Test
    void getTasksFiltersByAssigneeId() throws Exception {
        User assignee = createUser("assignee@example.com");
        User otherAssignee = createUser("other-assignee@example.com");
        TaskResponse assignedTask = createTask("Assigned task", null, assignee.getId(), adminToken());
        createTask("Other assigned task", null, otherAssignee.getId(), adminToken());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("assigneeId", assignee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(assignedTask.getId()))
                .andExpect(jsonPath("$.content[0].assigneeId").value(assignee.getId()));
    }

    @Test
    void changeStatusReturnsTaskWithNewStatus() throws Exception {
        TaskResponse task = createTask("Change status");
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/tasks/{id}/status", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void transitionStatusReturnsTransitionResponse() throws Exception {
        TaskResponse task = createTask("Transition status");
        TaskTransitionRequest request = new TaskTransitionRequest();
        request.setTargetStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/tasks/{id}/transitions", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(task.getId()))
                .andExpect(jsonPath("$.previousStatus").value("TODO"))
                .andExpect(jsonPath("$.currentStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.transitionedAt").exists());
    }

    @Test
    void transitionStatusRejectsInvalidTransition() throws Exception {
        TaskResponse task = createTask("Invalid transition");
        TaskTransitionRequest request = new TaskTransitionRequest();
        request.setTargetStatus(TaskStatus.DONE);

        mockMvc.perform(post("/api/tasks/{id}/transitions", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid task status transition: TODO -> DONE"));
    }

    @Test
    void transitionStatusRequiresReasonWhenPolicyRequiresIt() throws Exception {
        TaskResponse task = createTask("Reason transition");
        changeStatus(task.getId(), TaskStatus.IN_REVIEW);
        TaskTransitionRequest request = new TaskTransitionRequest();
        request.setTargetStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/tasks/{id}/transitions", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reason is required for transition: IN_REVIEW -> IN_PROGRESS"));
    }

    @Test
    void getTaskTimelineReturnsActivitiesForRequestedTask() throws Exception {
        TaskResponse task = createTask("Timeline task");
        createTask("Other timeline task");

        mockMvc.perform(get("/api/tasks/{id}/timeline", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value(TaskActivityType.TASK_CREATED.name()))
                .andExpect(jsonPath("$.content[0].actor.id").isNumber())
                .andExpect(jsonPath("$.content[0].message").value("Task created"));
    }

    @Test
    void getTaskTimelineRejectsUnauthorizedUser() throws Exception {
        TaskResponse task = createTask("Private timeline task", null, null, adminToken());

        mockMvc.perform(get("/api/tasks/{id}/timeline", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTaskTimelineSupportsPagination() throws Exception {
        TaskResponse task = createTask("Paged timeline task");
        changePriority(task.getId(), TaskPriority.HIGH);
        changeStatus(task.getId(), TaskStatus.IN_PROGRESS);

        mockMvc.perform(get("/api/tasks/{id}/timeline", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void createAcceptanceCriteriaReturnsCreatedCriteria() throws Exception {
        TaskResponse task = createTask("Criteria task");
        CreateAcceptanceCriteriaRequest request = acceptanceCriteriaRequest("Old refresh token becomes invalid.");

        mockMvc.perform(post("/api/tasks/{id}/acceptance-criteria", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.taskId").value(task.getId()))
                .andExpect(jsonPath("$.text").value("Old refresh token becomes invalid."))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdById").isNumber());
    }

    @Test
    void listAcceptanceCriteriaReturnsCriteriaForTask() throws Exception {
        TaskResponse task = createTask("List criteria task");
        TaskResponse otherTask = createTask("Other criteria task");
        createAcceptanceCriteria(task.getId(), "First criterion");
        createAcceptanceCriteria(otherTask.getId(), "Other criterion");

        mockMvc.perform(get("/api/tasks/{id}/acceptance-criteria", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("First criterion"));
    }

    @Test
    void updateAcceptanceCriteriaReturnsUpdatedCriteria() throws Exception {
        TaskResponse task = createTask("Update criteria task");
        AcceptanceCriteriaResponse criteria = createAcceptanceCriteria(task.getId(), "Original criterion");
        UpdateAcceptanceCriteriaRequest request = new UpdateAcceptanceCriteriaRequest();
        request.setText("Updated criterion");

        mockMvc.perform(patch("/api/tasks/{taskId}/acceptance-criteria/{criteriaId}", task.getId(), criteria.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated criterion"));
    }

    @Test
    void deleteAcceptanceCriteriaRemovesCriteria() throws Exception {
        TaskResponse task = createTask("Delete criteria task");
        AcceptanceCriteriaResponse criteria = createAcceptanceCriteria(task.getId(), "Delete me");

        mockMvc.perform(delete("/api/tasks/{taskId}/acceptance-criteria/{criteriaId}", task.getId(), criteria.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}/acceptance-criteria", task.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void completeAndReopenAcceptanceCriteriaUpdatesCompletionState() throws Exception {
        TaskResponse task = createTask("Complete criteria task");
        AcceptanceCriteriaResponse criteria = createAcceptanceCriteria(task.getId(), "Complete me");

        mockMvc.perform(patch("/api/tasks/{taskId}/acceptance-criteria/{criteriaId}/complete", task.getId(), criteria.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").exists())
                .andExpect(jsonPath("$.completedById").isNumber());

        mockMvc.perform(patch("/api/tasks/{taskId}/acceptance-criteria/{criteriaId}/reopen", task.getId(), criteria.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.completedAt").value(nullValue()))
                .andExpect(jsonPath("$.completedById").value(nullValue()));
    }

    @Test
    void bulkCreateAcceptanceCriteriaReturnsCreatedItems() throws Exception {
        TaskResponse task = createTask("Bulk criteria task");
        BulkCreateAcceptanceCriteriaRequest request = new BulkCreateAcceptanceCriteriaRequest();
        request.setItems(List.of(
                "Old refresh token becomes invalid.",
                "Expired refresh token returns 401."
        ));

        mockMvc.perform(post("/api/tasks/{id}/acceptance-criteria/bulk", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Old refresh token becomes invalid."))
                .andExpect(jsonPath("$[1].text").value("Expired refresh token returns 401."));
    }

    @Test
    void unauthorizedUserCannotManageAcceptanceCriteria() throws Exception {
        TaskResponse task = createTask("Private criteria task", null, null, adminToken());
        CreateAcceptanceCriteriaRequest request = acceptanceCriteriaRequest("Should be rejected");

        mockMvc.perform(post("/api/tasks/{id}/acceptance-criteria", task.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePriorityReturnsTaskWithNewPriority() throws Exception {
        TaskResponse task = createTask("Change priority");
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(TaskPriority.URGENT);

        mockMvc.perform(post("/api/tasks/{id}/priority", task.getId())
                        .header("Authorization", "Bearer " + userToken())
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
                        .header("Authorization", "Bearer " + userToken())
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(todoTask.getId()));
    }

    @Test
    void getTasksFiltersByPriority() throws Exception {
        TaskResponse mediumTask = createTask("Medium task");
        TaskResponse urgentTask = createTask("Urgent task");
        changePriority(urgentTask.getId(), TaskPriority.URGENT);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .param("priority", "URGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(urgentTask.getId()));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(mediumTask.getId()));
    }

    @Test
    void getTasksFiltersByType() throws Exception {
        TaskResponse featureTask = createTask("Feature task");
        CreateTaskRequest reliabilityRequest = createTaskRequest("Reliability task");
        reliabilityRequest.setType(TaskType.RELIABILITY);
        reliabilityRequest.setSeverity(Severity.CRITICAL);
        createTask(reliabilityRequest, userToken());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .param("type", "FEATURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(featureTask.getId()))
                .andExpect(jsonPath("$.content[0].type").value("FEATURE"));
    }

    @Test
    void getTasksFiltersBySeverity() throws Exception {
        CreateTaskRequest criticalRequest = createTaskRequest("Critical reliability task");
        criticalRequest.setType(TaskType.RELIABILITY);
        criticalRequest.setSeverity(Severity.CRITICAL);
        TaskResponse criticalTask = createTask(criticalRequest, userToken());
        CreateTaskRequest lowSeverityRequest = createTaskRequest("Low severity bug");
        lowSeverityRequest.setType(TaskType.BUG);
        lowSeverityRequest.setSeverity(Severity.LOW);
        createTask(lowSeverityRequest, userToken());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .param("severity", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(criticalTask.getId()))
                .andExpect(jsonPath("$.content[0].severity").value("CRITICAL"));
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
                        .header("Authorization", "Bearer " + userToken())
                        .param("status", "TODO")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(todoHighTask.getId()));
    }

    @Test
    void getTasksSearchesByTitle() throws Exception {
        TaskResponse invoiceTask = createTask("Review invoice");
        createTask("Plan roadmap");

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .param("search", "invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(invoiceTask.getId()));
    }

    @Test
    void getTasksFiltersByDueDateRange() throws Exception {
        TaskResponse firstJuneTask = createTask("First June task", LocalDate.of(2026, 6, 5));
        TaskResponse secondJuneTask = createTask("Second June task", LocalDate.of(2026, 6, 20));
        createTask("July task", LocalDate.of(2026, 7, 1));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .param("dueDateFrom", "2026-06-01")
                        .param("dueDateTo", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(
                        Math.toIntExact(firstJuneTask.getId()),
                        Math.toIntExact(secondJuneTask.getId())
                )));
    }

    @Test
    void getTasksFiltersFromTodayUntilDueDate() throws Exception {
        LocalDate today = LocalDate.now();
        createTask("Past task", today.minusDays(1));
        TaskResponse todayTask = createTask("Today task", today);
        TaskResponse upcomingTask = createTask("Upcoming task", today.plusDays(2));
        createTask("Too far task", today.plusDays(5));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .param("dueFromToday", "true")
                        .param("dueDateTo", today.plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(
                        Math.toIntExact(todayTask.getId()),
                        Math.toIntExact(upcomingTask.getId())
                )));
    }

    private TaskResponse createTask(String title) throws Exception {
        return createTask(title, null);
    }

    private TaskResponse createTask(String title, LocalDate dueDate) throws Exception {
        return createTask(title, dueDate, null);
    }

    private TaskResponse createTask(String title, LocalDate dueDate, Long assigneeId) throws Exception {
        return createTask(title, dueDate, assigneeId, userToken());
    }

    private TaskResponse createTask(String title, LocalDate dueDate, Long assigneeId, String token) throws Exception {
        return createTask(createTaskRequest(title, dueDate, assigneeId), token);
    }

    private TaskResponse createTask(CreateTaskRequest request, String token) throws Exception {
        String content = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, TaskResponse.class);
    }

    private CreateTaskRequest createTaskRequest(String title) {
        return createTaskRequest(title, null);
    }

    private CreateTaskRequest createTaskRequest(String title, LocalDate dueDate) {
        return createTaskRequest(title, dueDate, null);
    }

    private CreateTaskRequest createTaskRequest(String title, LocalDate dueDate, Long assigneeId) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(title);
        request.setDescription("Original description");
        request.setType(TaskType.FEATURE);
        request.setDueDate(dueDate);
        request.setAssigneeId(assigneeId);
        return request;
    }

    private User createUser(String email) {
        return createUser(email, Role.USER);
    }

    private User createUser(String email, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setUsername(email.substring(0, email.indexOf('@')));
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(role);
            return userRepository.save(user);
        });
    }

    private String adminToken() {
        User admin = createUser("admin@example.com", Role.ADMIN);
        return tokenFor(admin);
    }

    private String userToken() {
        User user = createUser("user@example.com", Role.USER);
        return tokenFor(user);
    }

    private String tokenFor(User user) {
        return jwtService.generateToken(user);
    }

    private void changeStatus(Long taskId, TaskStatus taskStatus) throws Exception {
        for (TaskStatus nextStatus : pathFromTodoTo(taskStatus)) {
            ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
            request.setStatus(nextStatus);
            if (TaskStatus.BLOCKED.equals(nextStatus) || TaskStatus.REOPENED.equals(nextStatus)) {
                request.setReason("Required transition reason");
            }
            mockMvc.perform(post("/api/tasks/{id}/status", taskId)
                            .header("Authorization", "Bearer " + userToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    private void changePriority(Long taskId, TaskPriority priority) throws Exception {
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(priority);
        mockMvc.perform(post("/api/tasks/{id}/priority", taskId)
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private AcceptanceCriteriaResponse createAcceptanceCriteria(Long taskId, String text) throws Exception {
        String content = mockMvc.perform(post("/api/tasks/{id}/acceptance-criteria", taskId)
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptanceCriteriaRequest(text))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, AcceptanceCriteriaResponse.class);
    }

    private CreateAcceptanceCriteriaRequest acceptanceCriteriaRequest(String text) {
        CreateAcceptanceCriteriaRequest request = new CreateAcceptanceCriteriaRequest();
        request.setText(text);
        return request;
    }

    private List<TaskStatus> pathFromTodoTo(TaskStatus status) {
        return switch (status) {
            case TODO -> List.of();
            case IN_PROGRESS -> List.of(TaskStatus.IN_PROGRESS);
            case IN_REVIEW -> List.of(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW);
            case BLOCKED -> List.of(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED);
            case DONE -> List.of(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW, TaskStatus.DONE);
            case REOPENED -> List.of(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW, TaskStatus.DONE, TaskStatus.REOPENED);
            case CANCELLED -> List.of(TaskStatus.CANCELLED);
        };
    }
}
