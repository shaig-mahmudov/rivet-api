package com.engine.taskmanagement.user.service;

import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
import com.engine.taskmanagement.user.dto.request.UpdateUserRequest;
import com.engine.taskmanagement.user.dto.response.UserResponse;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(UpdateUserRequest request);
}
