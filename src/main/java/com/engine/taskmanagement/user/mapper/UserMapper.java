package com.engine.taskmanagement.user.mapper;

import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
import com.engine.taskmanagement.user.dto.request.UpdateUserRequest;
import com.engine.taskmanagement.user.dto.response.UserResponse;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class UserMapper {
    public User toEntity(CreateUserRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());

        return response;
    }

}
