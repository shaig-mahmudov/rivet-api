package com.engine.taskmanagement.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiRequest {
    private String prompt;
    private int timeoutSeconds;
}
