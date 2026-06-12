package com.engine.taskmanagement.user.dto.response;

import com.engine.taskmanagement.auth.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
}
