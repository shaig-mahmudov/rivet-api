package com.engine.taskmanagement.project.controller;

import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.auth.service.JwtService;
import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getProjectsWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProjectReturnsCreatedProject() throws Exception {
        CreateProjectRequest request = createProjectRequest("Controller project", "Project API test");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Controller project"))
                .andExpect(jsonPath("$.description").value("Project API test"));
    }

    @Test
    void createProjectWithoutNameReturnsBadRequest() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setDescription("Missing name");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.name").value("Project Name is required"));
    }

    @Test
    void createProjectWithTooLongDescriptionReturnsBadRequest() throws Exception {
        CreateProjectRequest request = createProjectRequest("Project", "a".repeat(251));

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProjectsReturnsOnlyActiveProjects() throws Exception {
        ProjectResponse activeProject = createProject("Active project", "Visible");
        ProjectResponse deletedProject = createProject("Deleted project", "Hidden");
        mockMvc.perform(delete("/api/projects/{id}", deletedProject.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(activeProject.getId()))
                .andExpect(jsonPath("$.content[0].name").value("Active project"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getProjectsFiltersBySearch() throws Exception {
        ProjectResponse invoiceProject = createProject("Invoice rollout", "Internal tools");
        createProject("Roadmap", "Planning");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + userToken())
                        .param("search", "invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(invoiceProject.getId()));
    }

    @Test
    void getProjectsFiltersByOwnerId() throws Exception {
        User owner = createUser("owner@example.com");
        User otherOwner = createUser("other-owner@example.com");
        Project ownerProject = createProjectEntity("Owner project", owner);
        createProjectEntity("Other project", otherOwner);

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + userToken())
                        .param("ownerId", owner.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(ownerProject.getId()));
    }

    @Test
    void updateProjectReturnsUpdatedProject() throws Exception {
        ProjectResponse project = createProject("Old name", "Old description");
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("New name");
        request.setDescription("New description");

        mockMvc.perform(put("/api/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.description").value("New description"));
    }

    @Test
    void updateProjectWithoutNameReturnsBadRequest() throws Exception {
        ProjectResponse project = createProject("Old name", "Old description");
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setDescription("Missing name");

        mockMvc.perform(put("/api/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMissingProjectReturnsNotFound() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("New name");
        request.setDescription("New description");

        mockMvc.perform(put("/api/projects/{id}", 999L)
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProjectSoftDeletesProject() throws Exception {
        ProjectResponse project = createProject("Delete me", "Soft delete");
        TaskResponse task = createTask("Child task", project.getId());

        mockMvc.perform(delete("/api/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        assertThat(projectRepository.findByIdAndDeletedAtIsNull(project.getId())).isEmpty();
        assertThat(projectRepository.findByIdAndDeletedAtIsNotNull(project.getId())).isPresent();
        assertThat(taskRepository.findByIdAndDeletedAtIsNotNull(task.getId())).isPresent();
    }

    @Test
    void restoreProjectReturnsProjectToActiveList() throws Exception {
        ProjectResponse project = createProject("Restore me", "Soft deleted first");
        mockMvc.perform(delete("/api/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/projects/{id}/restore", project.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name").value("Restore me"));

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(project.getId()));
    }

    @Test
    void hardDeleteProjectRemovesProject() throws Exception {
        ProjectResponse project = createProject("Hard delete me", "Remove forever");

        mockMvc.perform(delete("/api/projects/{id}/hard", project.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());

        assertThat(projectRepository.findById(project.getId())).isEmpty();
    }

    @Test
    void hardDeleteProjectWithoutAdminReturnsUnauthorized() throws Exception {
        ProjectResponse project = createProject("Hard delete me", "Remove forever");

        mockMvc.perform(delete("/api/projects/{id}/hard", project.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMissingProjectReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/projects/{id}", 999L)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProjectTasksReturnsOnlyActiveTasksForProject() throws Exception {
        ProjectResponse project = createProject("Project A", "First project");
        ProjectResponse otherProject = createProject("Project B", "Second project");
        TaskResponse projectTask = createTask("Project task", project.getId());
        TaskResponse deletedProjectTask = createTask("Deleted project task", project.getId());
        createTask("Other project task", otherProject.getId());
        mockMvc.perform(delete("/api/tasks/{id}", deletedProjectTask.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{id}/tasks", project.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(projectTask.getId()))
                .andExpect(jsonPath("$.content[0].projectId").value(project.getId()));
    }

    @Test
    void getProjectTasksSupportsFiltersAndPathProjectWinsOverQueryProjectId() throws Exception {
        ProjectResponse project = createProject("Project A", "First project");
        ProjectResponse otherProject = createProject("Project B", "Second project");
        TaskResponse projectTodoTask = createTask("Project todo task", project.getId(), TaskStatus.TODO);
        createTask("Project done task", project.getId(), TaskStatus.DONE);
        createTask("Other project todo task", otherProject.getId(), TaskStatus.TODO);

        mockMvc.perform(get("/api/projects/{id}/tasks", project.getId())
                        .header("Authorization", "Bearer " + userToken())
                        .param("status", "TODO")
                        .param("projectId", otherProject.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(projectTodoTask.getId()))
                .andExpect(jsonPath("$.content[0].projectId").value(project.getId()));
    }

    @Test
    void getProjectTasksForMissingProjectReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/tasks", 999L)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProjectTasksForSoftDeletedProjectReturnsNotFound() throws Exception {
        ProjectResponse project = createProject("Deleted project", "No tasks should be listed");
        mockMvc.perform(delete("/api/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{id}/tasks", project.getId())
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isNotFound());
    }

    private ProjectResponse createProject(String name, String description) throws Exception {
        String content = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProjectRequest(name, description))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, ProjectResponse.class);
    }

    private TaskResponse createTask(String title, Long projectId) throws Exception {
        return createTask(title, projectId, null);
    }

    private TaskResponse createTask(String title, Long projectId, TaskStatus status) throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(title);
        request.setDescription("Original description");
        request.setProjectId(projectId);
        request.setStatus(status);

        String content = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, TaskResponse.class);
    }

    private CreateProjectRequest createProjectRequest(String name, String description) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email.substring(0, email.indexOf('@')));
        return userRepository.save(user);
    }

    private Project createProjectEntity(String name, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("Project description");
        project.setOwner(owner);
        return projectRepository.save(project);
    }

    private String adminToken() {
        User admin = new User();
        admin.setId(999L);
        admin.setEmail("admin@example.com");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        return jwtService.generateToken(admin);
    }

    private String userToken() {
        User user = new User();
        user.setId(998L);
        user.setEmail("user@example.com");
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        return jwtService.generateToken(user);
    }
}
