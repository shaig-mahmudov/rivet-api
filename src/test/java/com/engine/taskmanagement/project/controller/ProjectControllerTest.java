package com.engine.taskmanagement.project.controller;

import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void createProjectReturnsCreatedProject() throws Exception {
        CreateProjectRequest request = createProjectRequest("Controller project", "Project API test");

        mockMvc.perform(post("/api/projects")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProjectsReturnsOnlyActiveProjects() throws Exception {
        ProjectResponse activeProject = createProject("Active project", "Visible");
        ProjectResponse deletedProject = createProject("Deleted project", "Hidden");
        mockMvc.perform(delete("/api/projects/{id}", deletedProject.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(activeProject.getId()))
                .andExpect(jsonPath("$[0].name").value("Active project"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateProjectReturnsUpdatedProject() throws Exception {
        ProjectResponse project = createProject("Old name", "Old description");
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("New name");
        request.setDescription("New description");

        mockMvc.perform(put("/api/projects/{id}", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.description").value("New description"));
    }

    @Test
    void updateMissingProjectReturnsNotFound() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("New name");
        request.setDescription("New description");

        mockMvc.perform(put("/api/projects/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProjectSoftDeletesProject() throws Exception {
        ProjectResponse project = createProject("Delete me", "Soft delete");

        mockMvc.perform(delete("/api/projects/{id}", project.getId()))
                .andExpect(status().isNoContent());

        assertThat(projectRepository.findByIdAndDeletedAtIsNull(project.getId())).isEmpty();
        assertThat(projectRepository.findByIdAndDeletedAtIsNotNull(project.getId())).isPresent();
    }

    @Test
    void restoreProjectReturnsProjectToActiveList() throws Exception {
        ProjectResponse project = createProject("Restore me", "Soft deleted first");
        mockMvc.perform(delete("/api/projects/{id}", project.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/projects/{id}/restore", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name").value("Restore me"));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(project.getId()));
    }

    @Test
    void hardDeleteProjectRemovesProject() throws Exception {
        ProjectResponse project = createProject("Hard delete me", "Remove forever");

        mockMvc.perform(delete("/api/projects/{id}/hard", project.getId()))
                .andExpect(status().isNoContent());

        assertThat(projectRepository.findById(project.getId())).isEmpty();
    }

    @Test
    void deleteMissingProjectReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/projects/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    private ProjectResponse createProject(String name, String description) throws Exception {
        String content = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProjectRequest(name, description))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, ProjectResponse.class);
    }

    private CreateProjectRequest createProjectRequest(String name, String description) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }
}
