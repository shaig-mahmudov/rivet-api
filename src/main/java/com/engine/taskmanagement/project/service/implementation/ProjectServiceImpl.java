package com.engine.taskmanagement.project.service.implementation;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        Project project = projectMapper.toEntity(request);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    public Page<ProjectResponse> getProjects(FilterProjectRequest request, Pageable pageable) {
        return projectRepository.findAll(
                        ProjectSpecification.filter(request),
                        pageable)
                .map(projectMapper::toResponse);
    }

    @Override
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project currentProject = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));;
        projectMapper.updateEntity(request, currentProject);
        Project updatedProject = projectRepository.save(currentProject);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.markAsDeleted();
    }

    @Override
    @Transactional
    public void hardDeleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        projectRepository.delete(project);
    }

    @Transactional
    @Override
    public ProjectResponse restoreProject(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted Project Not Found with id: " + id));

        project.restore();
        return projectMapper.toResponse(project);
    }
}
