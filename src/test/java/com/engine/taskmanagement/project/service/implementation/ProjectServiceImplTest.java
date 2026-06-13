package com.engine.taskmanagement.project.service.implementation;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.FilterProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.project.service.abstraction.ProjectService;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.TaskType;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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

    @Autowired
    private UserRepository userRepository;

    private User currentUser;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        currentUser = createUser("current@example.com");
        authenticateAs(currentUser);
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

        assertThat(projectService.getProjects(null, Pageable.unpaged()))
                .extracting(ProjectResponse::getId)
                .containsExactly(activeProject.getId());
    }

    @Test
    void getProjectsFiltersBySearchAndExcludesSoftDeletedProjects() {
        ProjectResponse matchingName = projectService.createProject(
                createProjectRequest("Invoice rollout", "Internal tooling")
        );
        ProjectResponse matchingDescription = projectService.createProject(
                createProjectRequest("Finance cleanup", "Prepare invoice archive")
        );
        projectService.createProject(createProjectRequest("Roadmap", "Planning"));
        ProjectResponse deletedMatch = projectService.createProject(
                createProjectRequest("Old invoice", "Hidden")
        );
        projectService.deleteProject(deletedMatch.getId());
        FilterProjectRequest request = new FilterProjectRequest();
        request.setSearch("INVOICE");

        assertThat(projectService.getProjects(request, Pageable.unpaged()).getContent())
                .extracting(ProjectResponse::getId)
                .containsExactlyInAnyOrder(matchingName.getId(), matchingDescription.getId());
    }

    @Test
    void getProjectsFiltersByOwnerId() {
        User admin = createUser("admin@example.com", Role.ADMIN);
        authenticateAs(admin);
        User owner = createUser("owner@example.com");
        User otherOwner = createUser("other-owner@example.com");
        Project ownerProject = createProjectEntity("Owner project", owner);
        createProjectEntity("Other project", otherOwner);
        FilterProjectRequest request = new FilterProjectRequest();
        request.setOwnerId(owner.getId());

        assertThat(projectService.getProjects(request, Pageable.unpaged()).getContent())
                .extracting(ProjectResponse::getId)
                .containsExactly(ownerProject.getId());
    }

    @Test
    void getProjectsRejectsOtherOwnerForRegularUser() {
        User otherOwner = createUser("other-owner@example.com");
        createProjectEntity("Other project", otherOwner);
        FilterProjectRequest request = new FilterProjectRequest();
        request.setOwnerId(otherOwner.getId());

        assertThatThrownBy(() -> projectService.getProjects(request, Pageable.unpaged()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("own projects");
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
    void deleteProjectSoftDeletesActiveChildTasks() {
        ProjectResponse projectResponse = projectService.createProject(
                createProjectRequest("Project with tasks", "Archive children")
        );
        Project project = projectRepository.findById(projectResponse.getId()).orElseThrow();
        Task activeTask = createTask("Active child", project);
        Task alreadyDeletedTask = createTask("Already deleted child", project);
        alreadyDeletedTask.markAsDeleted();
        taskRepository.save(alreadyDeletedTask);

        projectService.deleteProject(project.getId());

        assertThat(taskRepository.findByIdAndDeletedAtIsNotNull(activeTask.getId())).isPresent();
        assertThat(taskRepository.findByIdAndDeletedAtIsNotNull(alreadyDeletedTask.getId())).isPresent();
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
        User admin = createUser("hard-delete-admin@example.com", Role.ADMIN);
        authenticateAs(admin);

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

    private User createUser(String email) {
        return createUser(email, Role.USER);
    }

    private User createUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email.substring(0, email.indexOf('@')));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project createProjectEntity(String name, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("Project description");
        project.setOwner(owner);
        return projectRepository.save(project);
    }

    private Task createTask(String title, Project project) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Task description");
        task.setType(TaskType.FEATURE);
        task.setProject(project);
        return taskRepository.save(task);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                )
        );
    }
}
