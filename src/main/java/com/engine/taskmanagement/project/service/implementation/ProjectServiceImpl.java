package com.engine.taskmanagement.project.service.implementation;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.FilterProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.mapper.ProjectMapper;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.project.service.abstraction.ProjectService;
import com.engine.taskmanagement.project.specification.ProjectSpecification;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final CurrentUserService currentUserService;

    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.currentUserService = currentUserService;
    }

    @Transactional
    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Project project = projectMapper.toEntity(request);
        project.setOwner(currentUser);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProjectResponse> getProjects(FilterProjectRequest request, Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();
        FilterProjectRequest effectiveRequest = scopeProjectFilter(request, currentUser);

        return projectRepository.findAll(
                        ProjectSpecification.filter(effectiveRequest),
                        pageable)
                .map(projectMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        requireProjectOwnerOrAdmin(project, currentUserService.getCurrentUser());
        return projectMapper.toResponse(project);
    }

    @Transactional
    @Override
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project currentProject = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        requireProjectOwnerOrAdmin(currentProject, currentUserService.getCurrentUser());
        projectMapper.updateEntity(request, currentProject);
        Project updatedProject = projectRepository.save(currentProject);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        requireProjectOwnerOrAdmin(project, currentUserService.getCurrentUser());

        project.markAsDeletedWithTasks();
    }

    @Override
    @Transactional
    public void hardDeleteProject(Long id) {
        requireAdmin(currentUserService.getCurrentUser());
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        projectRepository.delete(project);
    }

    @Transactional
    @Override
    public ProjectResponse restoreProject(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted Project Not Found with id: " + id));
        requireProjectOwnerOrAdmin(project, currentUserService.getCurrentUser());

        project.restore();
        return projectMapper.toResponse(project);
    }

    private FilterProjectRequest scopeProjectFilter(FilterProjectRequest request, User currentUser) {
        if (currentUserService.isAdmin(currentUser)) {
            return request;
        }

        Long currentUserId = currentUser.getId();
        if (request != null && request.getOwnerId() != null && !request.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You can only access your own projects");
        }

        FilterProjectRequest scopedRequest = request == null ? new FilterProjectRequest() : request;
        scopedRequest.setOwnerId(currentUserId);
        return scopedRequest;
    }

    private void requireProjectOwnerOrAdmin(Project project, User currentUser) {
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (project.getOwner() == null || !project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only manage your own projects");
        }
    }

    private void requireAdmin(User currentUser) {
        if (!currentUserService.isAdmin(currentUser)) {
            throw new ForbiddenException("Admin access is required");
        }
    }
}
