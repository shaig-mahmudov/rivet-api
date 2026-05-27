package com.engine.taskmanagement.project.service.implementation;

import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.mapper.ProjectMapper;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.project.service.abstraction.ProjectService;
import org.springframework.stereotype.Service;
import java.util.List;

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
        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAllByDeletedAtIsNotNull()
                .stream()
                .map(projectMapper::toResponse)
                .toList();
    }
}
