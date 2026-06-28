package com.engine.taskmanagement.project.service.abstraction;

import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.FilterProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request);
    Page<ProjectResponse> getProjects(FilterProjectRequest request, Pageable pageable);
    ProjectResponse getProjectById(Long id);
    ProjectResponse updateProject(Long id, UpdateProjectRequest request);
    void deleteProject(Long id);
    void hardDeleteProject(Long id);
    ProjectResponse restoreProject(Long id);
}
