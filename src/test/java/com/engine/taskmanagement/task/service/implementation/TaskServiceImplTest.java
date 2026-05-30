package com.engine.taskmanagement.task.service.implementation;

import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.task.dto.request.ChangeTaskPriorityRequest;
import com.engine.taskmanagement.task.dto.request.ChangeTaskStatusRequest;
import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
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

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
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

        assertThat(taskService.getAllTasks())
                .extracting(TaskResponse::getId)
                .containsExactly(activeTask.getId());
        assertThat(taskService.getDeletedTasks())
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
        assertThat(taskService.getAllTasks())
                .extracting(TaskResponse::getId)
                .containsExactly(task.getId());
        assertThat(taskService.getDeletedTasks()).isEmpty();
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

    private CreateTaskRequest createTaskRequest(String title) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(title);
        request.setDescription("Original description");
        return request;
    }
}
