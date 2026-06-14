package com.engine.taskmanagement.task.service.implementation;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.task.activity.entity.TaskActivity;
import com.engine.taskmanagement.task.activity.enums.TaskActivityType;
import com.engine.taskmanagement.task.activity.repository.TaskActivityRepository;
import com.engine.taskmanagement.task.criteria.dto.request.CreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.UpdateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.response.AcceptanceCriteriaResponse;
import com.engine.taskmanagement.task.criteria.service.abstraction.AcceptanceCriteriaService;
import com.engine.taskmanagement.task.dto.request.ChangeTaskPriorityRequest;
import com.engine.taskmanagement.task.dto.request.ChangeTaskStatusRequest;
import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
import com.engine.taskmanagement.task.dto.request.FilterTaskRequest;
import com.engine.taskmanagement.task.dto.request.PartialUpdateTaskRequest;
import com.engine.taskmanagement.task.dto.request.TaskTransitionRequest;
import com.engine.taskmanagement.task.dto.request.UpdateTaskRequest;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.dto.response.TaskTransitionResponse;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.Severity;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.enums.TaskType;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.task.service.abstraction.TaskService;
import com.engine.taskmanagement.task.service.abstraction.TaskStatusTransitionService;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceImplTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskStatusTransitionService taskStatusTransitionService;

    @Autowired
    private AcceptanceCriteriaService acceptanceCriteriaService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskActivityRepository taskActivityRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        currentUser = createUser("current@example.com");
        authenticateAs(currentUser);
    }

    @Test
    void createTaskUsesDefaultPriorityAndStatusWhenMissing() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Write tests");
        request.setDescription("Cover task service");
        request.setType(TaskType.TEST);

        TaskResponse response = taskService.createTask(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Write tests");
        assertThat(response.getType()).isEqualTo(TaskType.TEST);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void createTaskRecordsTaskCreatedActivity() {
        TaskResponse task = taskService.createTask(createTaskRequest("Activity task"));

        assertThat(activitiesFor(task.getId()))
                .extracting(TaskActivity::getType)
                .containsExactly(TaskActivityType.TASK_CREATED);
    }

    @Test
    void createTaskSavesTaskTypeAndTechnicalContext() {
        CreateTaskRequest request = createTaskRequest("Add refresh token rotation");
        request.setType(TaskType.FEATURE);
        request.setTechnicalContext("Spring Security JWT authentication module");
        request.setExpectedOutcome("Old refresh tokens become invalid after successful refresh.");

        TaskResponse response = taskService.createTask(request);

        assertThat(response.getType()).isEqualTo(TaskType.FEATURE);
        assertThat(response.getSeverity()).isNull();
        assertThat(response.getTechnicalContext()).isEqualTo("Spring Security JWT authentication module");
        assertThat(response.getExpectedOutcome()).isEqualTo("Old refresh tokens become invalid after successful refresh.");
    }

    @Test
    void createTaskThrowsWhenTypeIsMissing() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Missing type");
        request.setDescription("Type is required");

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Task type is required");
    }

    @Test
    void createIncidentTaskRequiresSeverity() {
        CreateTaskRequest request = createTaskRequest("Investigate production outage");
        request.setType(TaskType.INCIDENT);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Severity is required");
    }

    @Test
    void createReliabilityTaskRequiresSeverity() {
        CreateTaskRequest request = createTaskRequest("Fix duplicate order creation");
        request.setType(TaskType.RELIABILITY);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Severity is required");
    }

    @Test
    void createTaskAssignsActiveUser() {
        CreateTaskRequest request = createTaskRequest("Assigned task");
        request.setAssigneeId(currentUser.getId());

        TaskResponse response = taskService.createTask(request);

        assertThat(response.getAssigneeId()).isEqualTo(currentUser.getId());
    }

    @Test
    void createStandaloneTaskRejectsOtherAssigneeForRegularUser() {
        User assignee = createUser("assignee@example.com");
        CreateTaskRequest request = createTaskRequest("Assigned task");
        request.setAssigneeId(assignee.getId());

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("assign standalone tasks");
    }

    @Test
    void createTaskThrowsWhenAssigneeIsDeleted() {
        User assignee = createUser("deleted-assignee@example.com");
        assignee.markAsDeleted();
        userRepository.save(assignee);
        CreateTaskRequest request = createTaskRequest("Assigned task");
        request.setAssigneeId(assignee.getId());

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Active user not found");
    }

    @Test
    void getAllTasksExcludesSoftDeletedTasks() {
        TaskResponse activeTask = taskService.createTask(createTaskRequest("Active task"));
        TaskResponse deletedTask = taskService.createTask(createTaskRequest("Deleted task"));

        taskService.deleteTask(deletedTask.getId());

        assertThat(taskService.getTasks(null, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(activeTask.getId());
        assertThat(taskService.getDeletedTasks(Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(deletedTask.getId());
    }

    @Test
    void getTaskByIdThrowsWhenTaskIsSoftDeleted() {
        TaskResponse task = taskService.createTask(createTaskRequest("Temporary task"));

        taskService.deleteTask(task.getId());

        assertThatThrownBy(() -> taskService.getTaskById(task.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task Not Found");
    }

    @Test
    void updateTaskUpdatesAllEditableFields() {
        TaskResponse task = taskService.createTask(createTaskRequest("Old title"));
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New title");
        request.setDescription("New description");
        request.setType(TaskType.REFACTOR);
        request.setSeverity(Severity.LOW);
        request.setTechnicalContext("Task service update path");
        request.setExpectedOutcome("All editable task details are persisted");
        request.setPriority(TaskPriority.URGENT);
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setDueDate(LocalDate.now().plusDays(3));

        TaskResponse response = taskService.updateTask(task.getId(), request);

        assertThat(response.getTitle()).isEqualTo("New title");
        assertThat(response.getDescription()).isEqualTo("New description");
        assertThat(response.getType()).isEqualTo(TaskType.REFACTOR);
        assertThat(response.getSeverity()).isEqualTo(Severity.LOW);
        assertThat(response.getTechnicalContext()).isEqualTo("Task service update path");
        assertThat(response.getExpectedOutcome()).isEqualTo("All editable task details are persisted");
        assertThat(response.getPriority()).isEqualTo(TaskPriority.URGENT);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.getDueDate()).isEqualTo(request.getDueDate());
    }

    @Test
    void partialUpdateTaskOnlyUpdatesProvidedFields() {
        TaskResponse task = taskService.createTask(createTaskRequest("Original title"));
        PartialUpdateTaskRequest request = new PartialUpdateTaskRequest();
        request.setTitle("Patched title");
        request.setPriority(TaskPriority.HIGH);

        TaskResponse response = taskService.partialUpdateTask(task.getId(), request);

        assertThat(response.getTitle()).isEqualTo("Patched title");
        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.getDescription()).isEqualTo("Original description");
        assertThat(response.getType()).isEqualTo(TaskType.FEATURE);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void partialUpdateTaskRequiresSeverityWhenChangingToIncident() {
        TaskResponse task = taskService.createTask(createTaskRequest("Original title"));
        PartialUpdateTaskRequest request = new PartialUpdateTaskRequest();
        request.setType(TaskType.INCIDENT);

        assertThatThrownBy(() -> taskService.partialUpdateTask(task.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Severity is required");
    }

    @Test
    void restoreTaskMakesDeletedTaskActiveAgain() {
        TaskResponse task = taskService.createTask(createTaskRequest("Restore me"));
        taskService.deleteTask(task.getId());

        TaskResponse restored = taskService.restoreTask(task.getId());

        assertThat(restored.getId()).isEqualTo(task.getId());
        assertThat(taskService.getTasks(null, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(task.getId());
        assertThat(taskService.getDeletedTasks(Pageable.unpaged()).getContent()).isEmpty();
    }

    @Test
    void hardDeleteTaskRemovesTask() {
        TaskResponse task = taskService.createTask(createTaskRequest("Remove me"));
        User admin = createUser("hard-delete-admin@example.com", Role.ADMIN);
        authenticateAs(admin);

        taskService.hardDeleteTask(task.getId());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void changeTaskStatusUpdatesActiveTaskStatus() {
        TaskResponse task = taskService.createTask(createTaskRequest("Move status"));
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponse response = taskService.changeTaskStatus(task.getId(), request);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void changeTaskStatusRecordsActivity() {
        TaskResponse task = taskService.createTask(createTaskRequest("Move status activity"));
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        taskService.changeTaskStatus(task.getId(), request);

        assertThat(activitiesFor(task.getId()))
                .extracting(TaskActivity::getType)
                .containsExactlyInAnyOrder(TaskActivityType.STATUS_CHANGED, TaskActivityType.TASK_CREATED);
    }

    @Test
    void transitionTaskStatusAllowsConfiguredTransitions() {
        List<TransitionCase> transitions = List.of(
                new TransitionCase(TaskStatus.TODO, TaskStatus.IN_PROGRESS, null),
                new TransitionCase(TaskStatus.TODO, TaskStatus.CANCELLED, null),
                new TransitionCase(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW, null),
                new TransitionCase(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, "Waiting on dependency"),
                new TransitionCase(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED, null),
                new TransitionCase(TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS, null),
                new TransitionCase(TaskStatus.BLOCKED, TaskStatus.CANCELLED, null),
                new TransitionCase(TaskStatus.IN_REVIEW, TaskStatus.DONE, null),
                new TransitionCase(TaskStatus.IN_REVIEW, TaskStatus.IN_PROGRESS, "Review changes requested"),
                new TransitionCase(TaskStatus.IN_REVIEW, TaskStatus.BLOCKED, "Reviewer is blocked"),
                new TransitionCase(TaskStatus.DONE, TaskStatus.REOPENED, "Regression found"),
                new TransitionCase(TaskStatus.REOPENED, TaskStatus.IN_PROGRESS, null),
                new TransitionCase(TaskStatus.REOPENED, TaskStatus.CANCELLED, null)
        );

        for (TransitionCase transition : transitions) {
            TaskResponse task = taskService.createTask(createTaskRequest("Transition " + transition.to()));
            setStoredStatus(task.getId(), transition.from());
            TaskTransitionRequest request = transitionRequest(transition.to(), transition.reason());

            TaskTransitionResponse response = taskStatusTransitionService.transitionTaskStatus(task.getId(), request);

            assertThat(response.getTaskId()).isEqualTo(task.getId());
            assertThat(response.getPreviousStatus()).isEqualTo(transition.from());
            assertThat(response.getCurrentStatus()).isEqualTo(transition.to());
            assertThat(response.getTransitionedAt()).isNotNull();
        }
    }

    @Test
    void transitionTaskStatusRejectsInvalidTransitions() {
        List<TransitionCase> transitions = List.of(
                new TransitionCase(TaskStatus.TODO, TaskStatus.DONE, null),
                new TransitionCase(TaskStatus.DONE, TaskStatus.IN_PROGRESS, null),
                new TransitionCase(TaskStatus.CANCELLED, TaskStatus.IN_PROGRESS, null),
                new TransitionCase(TaskStatus.CANCELLED, TaskStatus.DONE, null)
        );

        for (TransitionCase transition : transitions) {
            TaskResponse task = taskService.createTask(createTaskRequest("Invalid transition " + transition.to()));
            setStoredStatus(task.getId(), transition.from());

            assertThatThrownBy(() -> taskStatusTransitionService.transitionTaskStatus(
                    task.getId(),
                    transitionRequest(transition.to(), transition.reason())
            ))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid task status transition");
        }
    }

    @Test
    void transitionTaskStatusRequiresDifferentTargetStatus() {
        TaskResponse task = taskService.createTask(createTaskRequest("Same status"));

        assertThatThrownBy(() -> taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.TODO, null)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Target status must be different");
    }

    @Test
    void transitionTaskStatusRequiresReasonForSelectedTransitions() {
        TaskResponse task = taskService.createTask(createTaskRequest("Blocked task"));
        setStoredStatus(task.getId(), TaskStatus.IN_PROGRESS);

        assertThatThrownBy(() -> taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.BLOCKED, null)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Reason is required");
    }

    @Test
    void transitionTaskStatusStoresValidTransitionWithReason() {
        TaskResponse task = taskService.createTask(createTaskRequest("Reopened task"));
        setStoredStatus(task.getId(), TaskStatus.DONE);

        TaskTransitionResponse response = taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.REOPENED, "Bug reproduced after release")
        );

        assertThat(response.getPreviousStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(response.getCurrentStatus()).isEqualTo(TaskStatus.REOPENED);
        assertThat(response.getReason()).isEqualTo("Bug reproduced after release");
    }

    @Test
    void transitionTaskStatusRejectsUnauthorizedUser() {
        User otherUser = createUser("other-owner@example.com");
        authenticateAs(otherUser);
        TaskResponse task = taskService.createTask(createTaskRequest("Private task"));
        authenticateAs(currentUser);

        assertThatThrownBy(() -> taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.IN_PROGRESS, null)
        ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("assigned to you or owned through your projects");
    }

    @Test
    void transitionTaskStatusAllowsProjectOwnerAndAssignee() {
        User assignee = createUser("transition-assignee@example.com");
        Project project = createProject("Transition project");
        TaskResponse task = taskService.createTask(createTaskRequest("Owned task", project.getId(), assignee.getId()));

        TaskTransitionResponse ownerResponse = taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.IN_PROGRESS, null)
        );
        authenticateAs(assignee);
        TaskTransitionResponse assigneeResponse = taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.IN_REVIEW, null)
        );

        assertThat(ownerResponse.getCurrentStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(assigneeResponse.getCurrentStatus()).isEqualTo(TaskStatus.IN_REVIEW);
    }

    @Test
    void transitionTaskStatusRejectsDoneWhenAcceptanceCriteriaAreIncomplete() {
        TaskResponse task = taskService.createTask(createTaskRequest("Incomplete criteria task"));
        setStoredStatus(task.getId(), TaskStatus.IN_REVIEW);
        acceptanceCriteriaService.create(task.getId(), acceptanceCriteriaRequest("Tests cover token reuse"));

        assertThatThrownBy(() -> taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.DONE, null)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("acceptance criteria are incomplete");
    }

    @Test
    void transitionTaskStatusAllowsDoneWhenAcceptanceCriteriaAreComplete() {
        TaskResponse task = taskService.createTask(createTaskRequest("Complete criteria task"));
        setStoredStatus(task.getId(), TaskStatus.IN_REVIEW);
        AcceptanceCriteriaResponse criteria = acceptanceCriteriaService.create(
                task.getId(),
                acceptanceCriteriaRequest("Tests cover token reuse")
        );
        acceptanceCriteriaService.complete(task.getId(), criteria.getId());

        TaskTransitionResponse response = taskStatusTransitionService.transitionTaskStatus(
                task.getId(),
                transitionRequest(TaskStatus.DONE, null)
        );

        assertThat(response.getCurrentStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void acceptanceCriteriaActionsRecordActivities() {
        TaskResponse task = taskService.createTask(createTaskRequest("Criteria activity task"));
        AcceptanceCriteriaResponse criteria = acceptanceCriteriaService.create(
                task.getId(),
                acceptanceCriteriaRequest("Original criterion")
        );
        UpdateAcceptanceCriteriaRequest updateRequest = new UpdateAcceptanceCriteriaRequest();
        updateRequest.setText("Updated criterion");

        acceptanceCriteriaService.update(task.getId(), criteria.getId(), updateRequest);
        acceptanceCriteriaService.complete(task.getId(), criteria.getId());
        acceptanceCriteriaService.reopen(task.getId(), criteria.getId());

        assertThat(activitiesFor(task.getId()))
                .extracting(TaskActivity::getType)
                .contains(
                        TaskActivityType.ACCEPTANCE_CRITERIA_ADDED,
                        TaskActivityType.ACCEPTANCE_CRITERIA_UPDATED,
                        TaskActivityType.ACCEPTANCE_CRITERIA_COMPLETED
                );
    }

    @Test
    void changeTaskPriorityUpdatesActiveTaskPriority() {
        TaskResponse task = taskService.createTask(createTaskRequest("Raise priority"));
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(TaskPriority.URGENT);

        TaskResponse response = taskService.changeTaskPriority(task.getId(), request);

        assertThat(response.getPriority()).isEqualTo(TaskPriority.URGENT);
    }

    @Test
    void changeTaskPriorityRecordsActivity() {
        TaskResponse task = taskService.createTask(createTaskRequest("Raise priority activity"));
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(TaskPriority.URGENT);

        taskService.changeTaskPriority(task.getId(), request);

        assertThat(activitiesFor(task.getId()))
                .extracting(TaskActivity::getType)
                .containsExactlyInAnyOrder(TaskActivityType.PRIORITY_CHANGED, TaskActivityType.TASK_CREATED);
    }

    @Test
    void partialUpdateTaskRecordsAssigneeTypeAndSeverityActivities() {
        User firstAssignee = createUser("first-assignee@example.com");
        User secondAssignee = createUser("second-assignee@example.com");
        Project project = createProject("Activity project");
        TaskResponse task = taskService.createTask(createTaskRequest("Update activity", project.getId(), firstAssignee.getId()));
        PartialUpdateTaskRequest request = new PartialUpdateTaskRequest();
        request.setAssigneeId(secondAssignee.getId());
        request.setType(TaskType.BUG);
        request.setSeverity(Severity.HIGH);

        taskService.partialUpdateTask(task.getId(), request);

        assertThat(activitiesFor(task.getId()))
                .extracting(TaskActivity::getType)
                .contains(
                        TaskActivityType.ASSIGNEE_CHANGED,
                        TaskActivityType.TYPE_CHANGED,
                        TaskActivityType.SEVERITY_CHANGED
                );
    }

    @Test
    void getTasksFiltersByStatusAndExcludesDeletedTasks() {
        TaskResponse todoTask = taskService.createTask(createTaskRequest("Todo task"));
        TaskResponse doneTask = taskService.createTask(createTaskRequest("Done task"));
        TaskResponse deletedTodoTask = taskService.createTask(createTaskRequest("Deleted todo task"));
        changeStatus(doneTask.getId(), TaskStatus.DONE);
        taskService.deleteTask(deletedTodoTask.getId());
        FilterTaskRequest request = new FilterTaskRequest();
        request.setStatus(TaskStatus.TODO);

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(todoTask.getId());
    }

    @Test
    void getTasksFiltersByPriorityAndExcludesDeletedTasks() {
        TaskResponse mediumTask = taskService.createTask(createTaskRequest("Medium task"));
        TaskResponse highTask = taskService.createTask(createTaskRequest("High task"));
        TaskResponse deletedHighTask = taskService.createTask(createTaskRequest("Deleted high task"));
        changePriority(highTask.getId(), TaskPriority.HIGH);
        changePriority(deletedHighTask.getId(), TaskPriority.HIGH);
        taskService.deleteTask(deletedHighTask.getId());
        FilterTaskRequest request = new FilterTaskRequest();
        request.setPriority(TaskPriority.HIGH);

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(highTask.getId());
        assertThat(taskService.getTasks(null, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .contains(mediumTask.getId());
    }

    @Test
    void getTasksFiltersByType() {
        TaskResponse featureTask = taskService.createTask(createTaskRequest("Feature task"));
        CreateTaskRequest reliabilityRequest = createTaskRequest("Reliability task");
        reliabilityRequest.setType(TaskType.RELIABILITY);
        reliabilityRequest.setSeverity(Severity.CRITICAL);
        taskService.createTask(reliabilityRequest);
        FilterTaskRequest request = new FilterTaskRequest();
        request.setType(TaskType.FEATURE);

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(featureTask.getId());
    }

    @Test
    void getTasksFiltersBySeverity() {
        CreateTaskRequest criticalRequest = createTaskRequest("Critical reliability task");
        criticalRequest.setType(TaskType.RELIABILITY);
        criticalRequest.setSeverity(Severity.CRITICAL);
        TaskResponse criticalTask = taskService.createTask(criticalRequest);
        CreateTaskRequest lowSeverityRequest = createTaskRequest("Low severity bug");
        lowSeverityRequest.setType(TaskType.BUG);
        lowSeverityRequest.setSeverity(Severity.LOW);
        taskService.createTask(lowSeverityRequest);
        FilterTaskRequest request = new FilterTaskRequest();
        request.setSeverity(Severity.CRITICAL);

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(criticalTask.getId());
    }

    @Test
    void getTasksFiltersByAssigneeId() {
        User assignee = createUser("assignee@example.com");
        User otherAssignee = createUser("other-assignee@example.com");
        Project project = createProject("Owned project");
        TaskResponse assignedTask = taskService.createTask(createTaskRequest("Assigned task", project.getId(), assignee.getId()));
        taskService.createTask(createTaskRequest("Other assigned task", project.getId(), otherAssignee.getId()));
        FilterTaskRequest request = new FilterTaskRequest();
        request.setAssigneeId(assignee.getId());

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(assignedTask.getId());
    }

    @Test
    void getTasksFiltersByStatusAndPriority() {
        TaskResponse todoHighTask = taskService.createTask(createTaskRequest("Todo high task"));
        TaskResponse doneHighTask = taskService.createTask(createTaskRequest("Done high task"));
        TaskResponse todoLowTask = taskService.createTask(createTaskRequest("Todo low task"));
        changePriority(todoHighTask.getId(), TaskPriority.HIGH);
        changePriority(doneHighTask.getId(), TaskPriority.HIGH);
        changeStatus(doneHighTask.getId(), TaskStatus.DONE);
        changePriority(todoLowTask.getId(), TaskPriority.LOW);
        FilterTaskRequest request = new FilterTaskRequest();
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(todoHighTask.getId());
    }

    @Test
    void getTasksSearchesTitleCaseInsensitivelyAndExcludesDeletedTasks() {
        TaskResponse invoiceTask = taskService.createTask(createTaskRequest("Review invoice"));
        taskService.createTask(createTaskRequest("Plan roadmap"));
        TaskResponse deletedInvoiceTask = taskService.createTask(createTaskRequest("Invoice archive"));
        taskService.deleteTask(deletedInvoiceTask.getId());
        FilterTaskRequest request = new FilterTaskRequest();
        request.setSearch("INVOICE");

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(invoiceTask.getId());
    }

    @Test
    void getTasksSearchesDescriptionAndCombinesWithPriority() {
        TaskResponse highPriorityTask = taskService.createTask(
                createTaskRequest("Backend cleanup", "Prepare release notes", null)
        );
        TaskResponse lowPriorityTask = taskService.createTask(
                createTaskRequest("Frontend cleanup", "Prepare release notes", null)
        );
        taskService.createTask(createTaskRequest("Meeting notes", "Plan next sprint", null));
        changePriority(highPriorityTask.getId(), TaskPriority.HIGH);
        changePriority(lowPriorityTask.getId(), TaskPriority.LOW);
        FilterTaskRequest request = new FilterTaskRequest();
        request.setSearch("release");
        request.setPriority(TaskPriority.HIGH);

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(highPriorityTask.getId());
    }

    @Test
    void getTasksFiltersByDueDateRange() {
        TaskResponse firstJuneTask = taskService.createTask(
                createTaskRequest("First June task", "Original description", LocalDate.of(2026, 6, 5))
        );
        TaskResponse secondJuneTask = taskService.createTask(
                createTaskRequest("Second June task", "Original description", LocalDate.of(2026, 6, 20))
        );
        taskService.createTask(
                createTaskRequest("July task", "Original description", LocalDate.of(2026, 7, 1))
        );
        FilterTaskRequest request = new FilterTaskRequest();
        request.setDueDateFrom(LocalDate.of(2026, 6, 1));
        request.setDueDateTo(LocalDate.of(2026, 6, 30));

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactlyInAnyOrder(firstJuneTask.getId(), secondJuneTask.getId());
    }

    @Test
    void getTasksFiltersFromTodayUntilDueDate() {
        LocalDate today = LocalDate.now();
        taskService.createTask(
                createTaskRequest("Past task", "Original description", today.minusDays(1))
        );
        TaskResponse todayTask = taskService.createTask(
                createTaskRequest("Today task", "Original description", today)
        );
        TaskResponse upcomingTask = taskService.createTask(
                createTaskRequest("Upcoming task", "Original description", today.plusDays(2))
        );
        taskService.createTask(
                createTaskRequest("Too far task", "Original description", today.plusDays(5))
        );
        FilterTaskRequest request = new FilterTaskRequest();
        request.setDueFromToday(true);
        request.setDueDateTo(today.plusDays(3));

        assertThat(taskService.getTasks(request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactlyInAnyOrder(todayTask.getId(), upcomingTask.getId());
    }

    @Test
    void getTasksByProjectIdReturnsOnlyActiveTasksForProject() {
        Project project = createProject("Project A");
        Project otherProject = createProject("Project B");
        TaskResponse projectTask = taskService.createTask(createTaskRequest("Project task", project.getId()));
        TaskResponse deletedProjectTask = taskService.createTask(createTaskRequest("Deleted project task", project.getId()));
        taskService.createTask(createTaskRequest("Other project task", otherProject.getId()));
        taskService.deleteTask(deletedProjectTask.getId());

        assertThat(taskService.getTasksByProjectId(project.getId(), null, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(projectTask.getId());
    }

    @Test
    void getTasksByProjectIdCombinesPathProjectWithFilters() {
        Project project = createProject("Project A");
        Project otherProject = createProject("Project B");
        TaskResponse todoTask = taskService.createTask(createTaskRequest("Todo project task", project.getId()));
        TaskResponse doneTask = taskService.createTask(createTaskRequest("Done project task", project.getId()));
        TaskResponse otherProjectTodoTask = taskService.createTask(createTaskRequest("Other project todo task", otherProject.getId()));
        changeStatus(doneTask.getId(), TaskStatus.DONE);
        FilterTaskRequest request = new FilterTaskRequest();
        request.setStatus(TaskStatus.TODO);

        assertThat(taskService.getTasksByProjectId(project.getId(), request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(todoTask.getId())
                .doesNotContain(otherProjectTodoTask.getId());
    }

    @Test
    void getTasksByProjectIdUsesPathProjectIdOverRequestProjectId() {
        Project project = createProject("Project A");
        Project otherProject = createProject("Project B");
        TaskResponse projectTask = taskService.createTask(createTaskRequest("Project task", project.getId()));
        taskService.createTask(createTaskRequest("Other project task", otherProject.getId()));
        FilterTaskRequest request = new FilterTaskRequest();
        request.setProjectId(otherProject.getId());

        assertThat(taskService.getTasksByProjectId(project.getId(), request, Pageable.unpaged()).getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(projectTask.getId());
    }

    @Test
    void getTasksByProjectIdThrowsWhenProjectDoesNotExist() {
        assertThatThrownBy(() -> taskService.getTasksByProjectId(999L, null, Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Active project not found");
    }

    @Test
    void getTasksByProjectIdThrowsWhenProjectIsSoftDeleted() {
        Project project = createProject("Deleted project");
        project.markAsDeleted();
        projectRepository.save(project);

        assertThatThrownBy(() -> taskService.getTasksByProjectId(project.getId(), null, Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Active project not found");
    }

    private CreateTaskRequest createTaskRequest(String title) {
        return createTaskRequest(title, "Original description", null);
    }

    private CreateTaskRequest createTaskRequest(String title, Long projectId) {
        CreateTaskRequest request = createTaskRequest(title);
        request.setProjectId(projectId);
        return request;
    }

    private CreateTaskRequest createTaskRequest(String title, Long projectId, Long assigneeId) {
        CreateTaskRequest request = createTaskRequest(title, projectId);
        request.setAssigneeId(assigneeId);
        return request;
    }

    private CreateTaskRequest createTaskRequest(String title, String description, LocalDate dueDate) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setType(TaskType.FEATURE);
        request.setDueDate(dueDate);
        return request;
    }

    private Project createProject(String name) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("Project description");
        project.setOwner(currentUser);
        return projectRepository.save(project);
    }

    private User createUser(String email) {
        return createUser(email, Role.USER);
    }

    private User createUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email.substring(0, email.indexOf('@')));
        user.setRole(role);
        return userRepository.save(user);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                )
        );
    }

    private void changeStatus(Long taskId, TaskStatus status) {
        for (TaskStatus nextStatus : pathFromTodoTo(status)) {
            ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
            request.setStatus(nextStatus);
            if (TaskStatus.BLOCKED.equals(nextStatus) || TaskStatus.REOPENED.equals(nextStatus)) {
                request.setReason("Required transition reason");
            }
            taskService.changeTaskStatus(taskId, request);
        }
    }

    private void changePriority(Long taskId, TaskPriority priority) {
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(priority);
        taskService.changeTaskPriority(taskId, request);
    }

    private void setStoredStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(status);
        taskRepository.save(task);
    }

    private List<TaskActivity> activitiesFor(Long taskId) {
        return taskActivityRepository.findByTaskId(
                taskId,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }

    private TaskTransitionRequest transitionRequest(TaskStatus targetStatus, String reason) {
        TaskTransitionRequest request = new TaskTransitionRequest();
        request.setTargetStatus(targetStatus);
        request.setReason(reason);
        return request;
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

    private record TransitionCase(TaskStatus from, TaskStatus to, String reason) {
    }
}
