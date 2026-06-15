package com.engine.taskmanagement.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiAcceptanceCriteriaDraftResponse {
    private Long taskId;
    private List<String> suggestions;
}
