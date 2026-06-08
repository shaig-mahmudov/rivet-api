package com.engine.taskmanagement.task.service.implementation;

import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.task.dto.request.ChangeTaskPriorityRequest;
import com.engine.taskmanagement.task.dto.request.ChangeTaskStatusRequest;
import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
import com.engine.taskmanagement.task.dto.request.FilterTaskRequest;
import com.engine.taskmanagement.task.dto.request.PartialUpdateTaskRequest;
import com.engine.taskmanagement.task.dto.request.UpdateTaskRequest;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.task.service.abstraction.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceImplTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void createTaskUsesDefaultPriorityAndStatusWhenMissing() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Write tests");
        request.setDescription("Cover task service");

        TaskResponse response = taskService.createTask(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Write tests");
        assertThat(response.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskRepository.findAll()).hasSize(1);
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
        request.setPriority(TaskPriority.URGENT);
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setDueDate(LocalDate.now().plusDays(3));

        TaskResponse response = taskService.updateTask(task.getId(), request);

        assertThat(response.getTitle()).isEqualTo("New title");
        assertThat(response.getDescription()).isEqualTo("New description");
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
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
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

        taskService.hardDeleteTask(task.getId());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void changeTaskStatusUpdatesActiveTaskStatus() {
        TaskResponse task = taskService.createTask(createTaskRequest("Move status"));
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
        request.setStatus(TaskStatus.DONE);

        TaskResponse response = taskService.changeTaskStatus(task.getId(), request);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.DONE);
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

    private CreateTaskRequest createTaskRequest(String title, String description, LocalDate dueDate) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setDueDate(dueDate);
        return request;
    }

    private Project createProject(String name) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("Project description");
        return projectRepository.save(project);
    }

    private void changeStatus(Long taskId, TaskStatus status) {
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest();
        request.setStatus(status);
        taskService.changeTaskStatus(taskId, request);
    }

    private void changePriority(Long taskId, TaskPriority priority) {
        ChangeTaskPriorityRequest request = new ChangeTaskPriorityRequest();
        request.setPriority(priority);
        taskService.changeTaskPriority(taskId, request);
    }
}
