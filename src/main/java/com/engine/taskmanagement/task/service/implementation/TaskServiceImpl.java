package com.engine.taskmanagement.task.service.implementation;

import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.task.dto.request.*;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.mapper.TaskMapper;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.task.service.abstraction.TaskService;
import com.engine.taskmanagement.task.specification.TaskSpecification;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;


    public TaskServiceImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskMapper taskMapper
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = taskMapper.toEntity(request);

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));

            task.setProject(project);
        }

        if (request.getAssigneeId() != null) {
            task.setAssignee(findActiveUser(request.getAssigneeId()));
        }

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    public Page<TaskResponse> getTasks(FilterTaskRequest request, Pageable pageable) {
        Page<Task> tasks = taskRepository.findAll(
                TaskSpecification.filter(request),
                pageable
        );

        return tasks.map(taskMapper::toResponse);
    }

    @Override
    public Page<TaskResponse> getDeletedTasks(Pageable pageable) {
        return taskRepository.findAllByDeletedAtIsNotNull(pageable)
                .map(taskMapper::toResponse);

    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found with id: " + id));

        return taskMapper.toResponse(task);
    }

    @Transactional
    @Override
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task currentTask = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));

            currentTask.setProject(project);
        }

        if (request.getAssigneeId() != null) {
            currentTask.setAssignee(findActiveUser(request.getAssigneeId()));
        }

        taskMapper.updateEntity(request, currentTask);
        Task updatedTask = taskRepository.save(currentTask);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse partialUpdateTask(Long id, PartialUpdateTaskRequest request) {
        Task currentTask = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id:" + id));

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));

            currentTask.setProject(project);
        }
        if (request.getAssigneeId() != null) {
            currentTask.setAssignee(findActiveUser(request.getAssigneeId()));
        }
        taskMapper.partialUpdateEntity(request, currentTask);
        Task updatedTask = taskRepository.save((currentTask));
        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found with id: " + id));

        task.markAsDeleted();
    }

    @Transactional
    @Override
    public void hardDeleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found with id: " + id));

        taskRepository.delete(task);
    }

    @Transactional
    @Override
    public TaskResponse restoreTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted Task Not Found with id: " + id));

        if (task.getProject() != null && task.getProject().getDeletedAt() != null) {
            throw new BadRequestException("Cannot restore task because its project is deleted");
        }

        task.restore();
        return taskMapper.toResponse(task);
    }

    @Transactional
    @Override
    public TaskResponse changeTaskStatus(Long id, ChangeTaskStatusRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        taskMapper.changeTaskStatus(request, task);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    @Override
    public TaskResponse changeTaskPriority(Long id, ChangeTaskPriorityRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        taskMapper.changeTaskPriority(request, task);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public Page<TaskResponse> getTasksByProjectId(Long projectId, FilterTaskRequest request, Pageable pageable) {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));

        if (request == null) {
            request = new FilterTaskRequest();
        }

        request.setProjectId(projectId);

        return taskRepository.findAll(TaskSpecification.filter(request), pageable)
                .map(taskMapper::toResponse);
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found"));
    }


}
