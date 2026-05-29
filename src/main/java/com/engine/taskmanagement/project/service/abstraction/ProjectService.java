package com.engine.taskmanagement.project.service.abstraction;

import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request);
    List<ProjectResponse> getAllProjects();
    ProjectResponse updateProject(Long id, UpdateProjectRequest request);
    void deleteProject(Long id);
}
