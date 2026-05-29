package com.engine.taskmanagement.task.service.implementation;

import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
import com.engine.taskmanagement.task.dto.request.PartialUpdateTaskRequest;
import com.engine.taskmanagement.task.dto.request.UpdateTaskRequest;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.mapper.TaskMapper;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.task.service.abstraction.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;


    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = taskMapper.toEntity(request);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    public List<TaskResponse> getDeletedTasks() {
        return taskRepository.findAllByDeletedAtIsNotNull()
                .stream()
                .map(taskMapper::toResponse)
                .toList();
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
        Task currentTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskMapper.updateEntity(currentTask, request);
        Task updatedTask = taskRepository.save(currentTask);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public TaskResponse partialUpdateTask(Long id, PartialUpdateTaskRequest request) {
        Task currentTask = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id:" + id));
        taskMapper.partialUpdateEntity(currentTask, request);
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

        if (task.getProject().getDeletedAt() != null) {
            throw new BadRequestException("Cannot restore task because its project is deleted");
        }

        task.restore();
        return taskMapper.toResponse(task);
    }

}
