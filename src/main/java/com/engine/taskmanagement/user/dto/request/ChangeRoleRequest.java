package com.engine.taskmanagement.user.dto.request;

import com.engine.taskmanagement.auth.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {
    @NotNull
    private Role role;
}
