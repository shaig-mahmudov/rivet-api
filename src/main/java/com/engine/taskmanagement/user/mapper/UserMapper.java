package com.engine.taskmanagement.user.mapper;

import com.engine.taskmanagement.auth.dto.request.RegisterRequest;
import com.engine.taskmanagement.user.dto.request.ChangeRoleRequest;
import com.engine.taskmanagement.user.dto.request.ChangeUsernameRequest;
import com.engine.taskmanagement.user.dto.request.CreateUserRequest;
import com.engine.taskmanagement.user.dto.response.UserResponse;
import com.engine.taskmanagement.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "username", source = "username")
    void changeUsername(ChangeUsernameRequest request, @MappingTarget User user);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "role", source = "role")
    void changeRole(ChangeRoleRequest request, @MappingTarget User user);

}
