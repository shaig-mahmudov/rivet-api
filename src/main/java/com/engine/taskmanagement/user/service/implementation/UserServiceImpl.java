package com.engine.taskmanagement.user.service.implementation;

import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
import com.engine.taskmanagement.user.dto.request.UpdateUserRequest;
import com.engine.taskmanagement.user.dto.response.UserResponse;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.mapper.UserMapper;
import com.engine.taskmanagement.user.repository.UserRepository;
import com.engine.taskmanagement.user.service.abstraction.UserService;

import java.util.List;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UpdateUserRequest request) {
        return null;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNotNull()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getAllDeletedUsers() {
        return List.of();
    }
}
