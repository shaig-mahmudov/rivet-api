package com.engine.taskmanagement.task.criteria.service.implementation;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.task.activity.service.abstraction.TaskActivityService;
import com.engine.taskmanagement.task.criteria.dto.request.BulkCreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.CreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.UpdateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.response.AcceptanceCriteriaResponse;
import com.engine.taskmanagement.task.criteria.entity.AcceptanceCriteria;
import com.engine.taskmanagement.task.criteria.repository.AcceptanceCriteriaRepository;
import com.engine.taskmanagement.task.criteria.service.abstraction.AcceptanceCriteriaService;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AcceptanceCriteriaServiceImpl implements AcceptanceCriteriaService {

    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final TaskActivityService taskActivityService;

    public AcceptanceCriteriaServiceImpl(
            AcceptanceCriteriaRepository acceptanceCriteriaRepository,
            TaskRepository taskRepository,
            CurrentUserService currentUserService,
            TaskActivityService taskActivityService
    ) {
        this.acceptanceCriteriaRepository = acceptanceCriteriaRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.taskActivityService = taskActivityService;
    }

    @Override
    @Transactional
    public AcceptanceCriteriaResponse create(Long taskId, CreateAcceptanceCriteriaRequest request) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        criteria.setTask(task);
        criteria.setText(request.getText().trim());
        criteria.setCreatedBy(currentUser);

        AcceptanceCriteria savedCriteria = acceptanceCriteriaRepository.save(criteria);
        taskActivityService.recordAcceptanceCriteriaAdded(task, currentUser, savedCriteria.getText());
        return toResponse(savedCriteria);
    }

    @Override
    @Transactional
    public List<AcceptanceCriteriaResponse> bulkCreate(Long taskId, BulkCreateAcceptanceCriteriaRequest request) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();

        return request.getItems().stream()
                .map(String::trim)
                .map(text -> createCriteria(task, currentUser, text))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcceptanceCriteriaResponse> list(Long taskId) {
        findTaskForCurrentUser(taskId);
        return acceptanceCriteriaRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AcceptanceCriteriaResponse update(Long taskId, Long criteriaId, UpdateAcceptanceCriteriaRequest request) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        AcceptanceCriteria criteria = findCriteria(taskId, criteriaId);
        String oldText = criteria.getText();
        criteria.setText(request.getText().trim());

        AcceptanceCriteria savedCriteria = acceptanceCriteriaRepository.save(criteria);
        taskActivityService.recordAcceptanceCriteriaUpdated(task, currentUser, oldText, savedCriteria.getText());
        return toResponse(savedCriteria);
    }

    @Override
    @Transactional
    public AcceptanceCriteriaResponse complete(Long taskId, Long criteriaId) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        AcceptanceCriteria criteria = findCriteria(taskId, criteriaId);

        if (!criteria.isCompleted()) {
            criteria.setCompleted(true);
            criteria.setCompletedAt(LocalDateTime.now());
            criteria.setCompletedBy(currentUser);
            taskActivityService.recordAcceptanceCriteriaCompleted(task, currentUser, criteria.getText());
        }

        return toResponse(acceptanceCriteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public AcceptanceCriteriaResponse reopen(Long taskId, Long criteriaId) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        AcceptanceCriteria criteria = findCriteria(taskId, criteriaId);

        if (criteria.isCompleted()) {
            criteria.setCompleted(false);
            criteria.setCompletedAt(null);
            criteria.setCompletedBy(null);
            taskActivityService.recordAcceptanceCriteriaReopened(task, currentUser, criteria.getText());
        }

        return toResponse(acceptanceCriteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public void delete(Long taskId, Long criteriaId) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        AcceptanceCriteria criteria = findCriteria(taskId, criteriaId);

        acceptanceCriteriaRepository.delete(criteria);
        taskActivityService.recordAcceptanceCriteriaDeleted(task, currentUser, criteria.getText());
    }

    private AcceptanceCriteria createCriteria(Task task, User currentUser, String text) {
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        criteria.setTask(task);
        criteria.setText(text);
        criteria.setCreatedBy(currentUser);

        AcceptanceCriteria savedCriteria = acceptanceCriteriaRepository.save(criteria);
        taskActivityService.recordAcceptanceCriteriaAdded(task, currentUser, savedCriteria.getText());
        return savedCriteria;
    }

    private Task findTaskForCurrentUser(Long taskId) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        requireTaskAccess(task, currentUserService.getCurrentUser());
        return task;
    }

    private AcceptanceCriteria findCriteria(Long taskId, Long criteriaId) {
        return acceptanceCriteriaRepository.findByIdAndTaskId(criteriaId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Acceptance criteria not found with id: " + criteriaId));
    }

    private AcceptanceCriteriaResponse toResponse(AcceptanceCriteria criteria) {
        AcceptanceCriteriaResponse response = new AcceptanceCriteriaResponse();
        response.setId(criteria.getId());
        response.setTaskId(criteria.getTask().getId());
        response.setText(criteria.getText());
        response.setCompleted(criteria.isCompleted());
        response.setCreatedById(criteria.getCreatedBy() == null ? null : criteria.getCreatedBy().getId());
        response.setCreatedAt(criteria.getCreatedAt());
        response.setUpdatedAt(criteria.getUpdatedAt());
        response.setCompletedAt(criteria.getCompletedAt());
        response.setCompletedById(criteria.getCompletedBy() == null ? null : criteria.getCompletedBy().getId());
        return response;
    }

    private void requireTaskAccess(Task task, User currentUser) {
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (isAssignee(task, currentUser) || isProjectOwner(task.getProject(), currentUser)) {
            return;
        }
        throw new ForbiddenException("You can only access tasks assigned to you or owned through your projects");
    }

    private boolean isAssignee(Task task, User currentUser) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
    }

    private boolean isProjectOwner(Project project, User currentUser) {
        return project != null && project.getOwner() != null && project.getOwner().getId().equals(currentUser.getId());
    }
}
