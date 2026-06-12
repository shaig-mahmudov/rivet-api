package com.engine.taskmanagement.auth.controller;

import com.engine.taskmanagement.auth.dto.request.LoginRequest;
import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesUserWithEncodedPassword() throws Exception {
        CreateUserRequest request = createUserRequest("new-user@example.com", "password123");
        request.setRole(Role.ADMIN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.user.email").value("new-user@example.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));

        String storedPassword = userRepository.findByEmail("new-user@example.com").orElseThrow().getPassword();
        assertThat(passwordEncoder.matches("password123", storedPassword)).isTrue();
    }

    @Test
    void loginReturnsUserForValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest("login@example.com", "password123"))))
                .andExpect(status().isCreated());
        LoginRequest request = new LoginRequest();
        request.setEmail("login@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("login@example.com"));
    }

    @Test
    void registerWithPasswordMismatchReturnsBadRequest() throws Exception {
        CreateUserRequest request = createUserRequest("bad-user@example.com", "password123");
        request.setConfirmPassword("different");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private CreateUserRequest createUserRequest(String email, String password) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(email.substring(0, email.indexOf('@')));
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
    }
}
