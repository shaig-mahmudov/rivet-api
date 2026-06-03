package com.engine.taskmanagement.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUsernameRequest {
    @NotBlank
    private String username;
}
