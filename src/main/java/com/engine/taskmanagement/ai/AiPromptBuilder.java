package com.engine.taskmanagement.ai;

import com.engine.taskmanagement.task.criteria.entity.AcceptanceCriteria;
import com.engine.taskmanagement.task.entity.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiPromptBuilder {

    public String buildAcceptanceCriteriaPrompt(Task task, List<AcceptanceCriteria> existingCriteria) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate 3-6 clear, testable acceptance criteria for this engineering task.\n");
        prompt.append("Each criterion should be specific, measurable, and implementation-focused.\n");
        prompt.append("Do not include vague criteria.\n");
        prompt.append("Do not include explanations.\n");
        prompt.append("Return only a JSON array of strings.\n\n");
        prompt.append("Task title: ").append(value(task.getTitle())).append('\n');
        prompt.append("Task description: ").append(value(task.getDescription())).append('\n');
        prompt.append("Task type: ").append(value(task.getType())).append('\n');
        prompt.append("Priority: ").append(value(task.getPriority())).append('\n');
        prompt.append("Severity: ").append(value(task.getSeverity())).append('\n');
        prompt.append("Technical context: ").append(value(task.getTechnicalContext())).append('\n');
        prompt.append("Expected outcome: ").append(value(task.getExpectedOutcome())).append('\n');
        prompt.append("Project name: ").append(task.getProject() == null ? "None" : value(task.getProject().getName())).append('\n');
        prompt.append("Existing acceptance criteria:\n");
        if (existingCriteria.isEmpty()) {
            prompt.append("- None\n");
        } else {
            existingCriteria.forEach(criteria -> prompt.append("- ").append(criteria.getText()).append('\n'));
        }
        return prompt.toString();
    }

    private String value(Object value) {
        return value == null ? "None" : value.toString();
    }
}
