package com.engine.taskmanagement.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/ai")
public class AiSuggestionController {

    private final AiAcceptanceCriteriaService aiAcceptanceCriteriaService;

    public AiSuggestionController(AiAcceptanceCriteriaService aiAcceptanceCriteriaService) {
        this.aiAcceptanceCriteriaService = aiAcceptanceCriteriaService;
    }

    @PostMapping("/acceptance-criteria/draft")
    public ResponseEntity<AiAcceptanceCriteriaDraftResponse> generateAcceptanceCriteriaDraft(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(aiAcceptanceCriteriaService.generateAcceptanceCriteriaDraft(taskId));
    }
}
