package com.engine.taskmanagement.user.service.implementation;

import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
import com.engine.taskmanagement.user.dto.request.UpdateUserRequest;
import com.engine.taskmanagement.user.dto.response.UserResponse;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.mapper.UserMapper;
import com.engine.taskmanagement.user.repository.UserRepository;
import com.engine.taskmanagement.user.service.abstraction.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(UpdateUserRequest request) {
        throw new UnsupportedOperationException("User update is not implemented yet");
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getAllDeletedUsers() {
        return userRepository.findAllByDeletedAtIsNotNull()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
