package com.engine.taskmanagement.user.dto.response;

import com.engine.taskmanagement.auth.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long Id;
    private String username;
    private Role role;
}
