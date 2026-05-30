package com.engine.taskmanagement.project.controller;

import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.service.abstraction.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> response = projectService.getAllProjects();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProjectRequest request
    ) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteProject(@PathVariable Long id) {
        projectService.hardDeleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    private ResponseEntity<ProjectResponse> restoreProject(@PathVariable Long id) {
        ProjectResponse response = projectService.restoreProject(id);
        return ResponseEntity.ok(response);
    }

}
