package com.engine.taskmanagement.auth.controller;

import com.engine.taskmanagement.auth.dto.request.AdminBootstrapRequest;
import com.engine.taskmanagement.auth.dto.request.LoginRequest;
import com.engine.taskmanagement.auth.dto.request.RefreshTokenRequest;
import com.engine.taskmanagement.auth.dto.request.RegisterRequest;
import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.auth.token.repository.RefreshTokenRepository;
import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
import com.engine.taskmanagement.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

@SpringBootTest(properties = "app.security.admin-bootstrap.token=test-bootstrap-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BOOTSTRAP_TOKEN = "test-bootstrap-token";

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void bootstrapAdminCreatesFirstAdmin() throws Exception {
        AdminBootstrapRequest request = adminBootstrapRequest("bootstrap-admin@example.com", "password123", BOOTSTRAP_TOKEN);

        mockMvc.perform(post("/api/auth/bootstrap/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Admin bootstrap successful"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.user.email").value("bootstrap-admin@example.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));

        assertThat(userRepository.findByEmail("bootstrap-admin@example.com").orElseThrow().getRole())
                .isEqualTo(Role.ADMIN);
        assertThat(refreshTokenRepository.findAll()).hasSize(1);
    }

    @Test
    void bootstrapAdminRejectsInvalidToken() throws Exception {
        AdminBootstrapRequest request = adminBootstrapRequest("invalid-token@example.com", "password123", "wrong-token");

        mockMvc.perform(post("/api/auth/bootstrap/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByEmail("invalid-token@example.com")).isEmpty();
    }

    @Test
    void bootstrapAdminRejectsWhenAdminAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/auth/bootstrap/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminBootstrapRequest(
                                "first-admin@example.com",
                                "password123",
                                BOOTSTRAP_TOKEN
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/bootstrap/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminBootstrapRequest(
                                "second-admin@example.com",
                                "password123",
                                BOOTSTRAP_TOKEN
                        ))))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByEmail("second-admin@example.com")).isEmpty();
    }

    @Test
    void bootstrapAdminRejectsPasswordMismatch() throws Exception {
        AdminBootstrapRequest request = adminBootstrapRequest("mismatch-admin@example.com", "password123", BOOTSTRAP_TOKEN);
        request.setConfirmPassword("different");

        mockMvc.perform(post("/api/auth/bootstrap/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCreatesUserWithEncodedPassword() throws Exception {
        RegisterRequest request = registerRequest("new-user@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.refreshExpiresIn").isNumber())
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
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("login@example.com"));
    }

    @Test
    void loginStoresOnlyHashedRefreshToken() throws Exception {
        registerUser("hashed-refresh@example.com", "password123");
        LoginRequest request = loginRequest("hashed-refresh@example.com", "password123");

        JsonNode response = performLogin(request);
        String refreshToken = response.get("refreshToken").asText();

        assertThat(refreshTokenRepository.findAll())
                .hasSize(2)
                .noneMatch(token -> token.getTokenHash().equals(refreshToken));
    }

    @Test
    void refreshRotatesRefreshToken() throws Exception {
        JsonNode loginResponse = registerUser("rotate@example.com", "password123");
        String oldRefreshToken = loginResponse.get("refreshToken").asText();

        JsonNode refreshResponse = performRefresh(oldRefreshToken);
        String newRefreshToken = refreshResponse.get("refreshToken").asText();

        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest(oldRefreshToken))))
                .andExpect(status().isUnauthorized());

        performRefresh(newRefreshToken);
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        JsonNode loginResponse = registerUser("logout@example.com", "password123");
        String refreshToken = loginResponse.get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest(refreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest("invalid-refresh-token"))))
                .andExpect(status().isUnauthorized());
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

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(email.substring(0, email.indexOf('@')));
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
    }

    private AdminBootstrapRequest adminBootstrapRequest(String email, String password, String bootstrapToken) {
        AdminBootstrapRequest request = new AdminBootstrapRequest();
        request.setUsername(email.substring(0, email.indexOf('@')));
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        request.setBootstrapToken(bootstrapToken);
        return request;
    }

    private CreateUserRequest createUserRequest(String email, String password) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(email.substring(0, email.indexOf('@')));
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private RefreshTokenRequest refreshTokenRequest(String token) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(token);
        return request;
    }

    private JsonNode registerUser(String email, String password) throws Exception {
        String content = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest(email, password))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode performLogin(LoginRequest request) throws Exception {
        String content = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode performRefresh(String refreshToken) throws Exception {
        String content = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }
}
