package com.engine.taskmanagement.project.service.implementation;

import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.project.service.abstraction.ProjectService;
import com.engine.taskmanagement.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ProjectServiceImplTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void createProjectSavesAndReturnsProject() {
        CreateProjectRequest request = createProjectRequest("MVP", "First prototype");

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("MVP");
        assertThat(response.getDescription()).isEqualTo("First prototype");
        assertThat(projectRepository.findAll()).hasSize(1);
    }

    @Test
    void getAllProjectsExcludesSoftDeletedProjects() {
        ProjectResponse activeProject = projectService.createProject(
                createProjectRequest("Active project", "Visible")
        );
        ProjectResponse deletedProject = projectService.createProject(
                createProjectRequest("Deleted project", "Hidden")
        );

        projectService.deleteProject(deletedProject.getId());

        assertThat(projectService.getAllProjects())
                .extracting(ProjectResponse::getId)
                .containsExactly(activeProject.getId());
    }

    @Test
    void updateProjectUpdatesActiveProject() {
        ProjectResponse project = projectService.createProject(
                createProjectRequest("Old name", "Old description")
        );
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("New name");
        request.setDescription("New description");

        ProjectResponse response = projectService.updateProject(project.getId(), request);

        assertThat(response.getId()).isEqualTo(project.getId());
        assertThat(response.getName()).isEqualTo("New name");
        assertThat(response.getDescription()).isEqualTo("New description");
    }

    @Test
    void updateProjectThrowsWhenProjectIsDeleted() {
        ProjectResponse project = projectService.createProject(
                createProjectRequest("Deleted project", "Cannot update")
        );
        projectService.deleteProject(project.getId());
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("New name");
        request.setDescription("New description");

        assertThatThrownBy(() -> projectService.updateProject(project.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void deleteProjectSoftDeletesProject() {
        ProjectResponse project = projectService.createProject(
                createProjectRequest("Soft delete", "Archive this")
        );

        projectService.deleteProject(project.getId());

        assertThat(projectRepository.findByIdAndDeletedAtIsNull(project.getId())).isEmpty();
        assertThat(projectRepository.findByIdAndDeletedAtIsNotNull(project.getId())).isPresent();
    }

    @Test
    void restoreProjectMakesDeletedProjectActiveAgain() {
        ProjectResponse project = projectService.createProject(
                createProjectRequest("Restore", "Bring back")
        );
        projectService.deleteProject(project.getId());

        ProjectResponse restored = projectService.restoreProject(project.getId());

        assertThat(restored.getId()).isEqualTo(project.getId());
        assertThat(projectRepository.findByIdAndDeletedAtIsNull(project.getId())).isPresent();
        assertThat(projectRepository.findByIdAndDeletedAtIsNotNull(project.getId())).isEmpty();
    }

    @Test
    void hardDeleteProjectRemovesProject() {
        ProjectResponse project = projectService.createProject(
                createProjectRequest("Hard delete", "Remove forever")
        );

        projectService.hardDeleteProject(project.getId());

        assertThat(projectRepository.findById(project.getId())).isEmpty();
    }

    @Test
    void deleteProjectThrowsWhenProjectDoesNotExist() {
        assertThatThrownBy(() -> projectService.deleteProject(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    private CreateProjectRequest createProjectRequest(String name, String description) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }
}
