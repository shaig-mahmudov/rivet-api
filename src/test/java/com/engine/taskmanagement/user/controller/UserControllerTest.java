package com.engine.taskmanagement.user.controller;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.auth.service.JwtService;
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

    @BeforeEach
    void setUp() {
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
                .andExpect(jsonPath("$[0].id").value(activeUser.getId()))
                .andExpect(jsonPath("$[0].email").value("active@example.com"));

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
        User admin = new User();
        admin.setId(999L);
        admin.setEmail("admin@example.com");
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        return jwtService.generateToken(admin);
    }
}
