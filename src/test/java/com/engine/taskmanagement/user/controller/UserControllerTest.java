package com.engine.taskmanagement.user.controller;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.auth.service.JwtService;
import com.engine.taskmanagement.auth.token.repository.RefreshTokenRepository;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
import com.engine.taskmanagement.user.dto.request.UpdateUserRequest;
import com.engine.taskmanagement.user.dto.response.UserResponse;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        CreateUserRequest request = createUserRequest("user@example.com");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUsersReturnsOnlyActiveUsers() throws Exception {
        UserResponse activeUser = createUser("active@example.com");
        UserResponse deletedUser = createUser("deleted@example.com");
        mockMvc.perform(delete("/api/users/{id}", deletedUser.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/users/{id}", deletedUser.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(Math.toIntExact(activeUser.getId()))))
                .andExpect(jsonPath("$[*].id").value(not(hasItem(Math.toIntExact(deletedUser.getId())))));

        mockMvc.perform(get("/api/users/deleted")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(deletedUser.getId()));
    }

    @Test
    void updateUserChangesEditableFields() throws Exception {
        UserResponse user = createUser("old@example.com");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("updated");
        request.setEmail("updated@example.com");
        request.setRole(Role.ADMIN);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void restoreUserReturnsUserToActiveList() throws Exception {
        UserResponse user = createUser("restore@example.com");
        mockMvc.perform(delete("/api/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/users/{id}/restore", user.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));

        assertThat(userRepository.findByIdAndDeletedAtIsNull(user.getId())).isPresent();
    }

    @Test
    void deletedAdminTokenCannotAccessAdminEndpoints() throws Exception {
        User admin = adminUser();
        String token = jwtService.generateToken(admin);
        admin.markAsDeleted();
        userRepository.save(admin);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void demotedAdminTokenUsesCurrentRole() throws Exception {
        User admin = adminUser();
        String token = jwtService.generateToken(admin);
        admin.setRole(Role.USER);
        userRepository.save(admin);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private UserResponse createUser(String email) throws Exception {
        String content = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest(email))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, UserResponse.class);
    }

    private CreateUserRequest createUserRequest(String email) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(email.substring(0, email.indexOf('@')));
        request.setEmail(email);
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        return request;
    }

    private String adminToken() {
        return jwtService.generateToken(adminUser());
    }

    private User adminUser() {
        return userRepository.findByEmail("admin@example.com").orElseGet(() -> {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setUsername("admin");
            admin.setRole(Role.ADMIN);
            return userRepository.save(admin);
        });
    }
}
