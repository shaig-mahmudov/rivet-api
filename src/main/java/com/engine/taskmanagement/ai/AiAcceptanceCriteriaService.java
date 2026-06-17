package com.engine.taskmanagement.ai;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.task.criteria.entity.AcceptanceCriteria;
import com.engine.taskmanagement.task.criteria.repository.AcceptanceCriteriaRepository;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AiAcceptanceCriteriaService {

    private static final int TIMEOUT_SECONDS = 10;
    private static final int MAX_SUGGESTION_LENGTH = 500;
    private static final int MIN_SUGGESTIONS = 3;
    private static final int MAX_SUGGESTIONS = 8;

    private final AiProvider aiProvider;
    private final AiPromptBuilder aiPromptBuilder;
    private final TaskRepository taskRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final CurrentUserService currentUserService;
    private final TransactionTemplate readOnlyTransaction;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiAcceptanceCriteriaService(
            AiProvider aiProvider,
            AiPromptBuilder aiPromptBuilder,
            TaskRepository taskRepository,
            AcceptanceCriteriaRepository acceptanceCriteriaRepository,
            CurrentUserService currentUserService,
            PlatformTransactionManager transactionManager
    ) {
        this.aiProvider = aiProvider;
        this.aiPromptBuilder = aiPromptBuilder;
        this.taskRepository = taskRepository;
        this.acceptanceCriteriaRepository = acceptanceCriteriaRepository;
        this.currentUserService = currentUserService;
        this.readOnlyTransaction = new TransactionTemplate(transactionManager);
        this.readOnlyTransaction.setReadOnly(true);
    }

    public AiAcceptanceCriteriaDraftResponse generateAcceptanceCriteriaDraft(Long taskId) {
        PromptData promptData = readOnlyTransaction.execute(status -> loadPromptData(taskId));
        if (promptData == null) {
            throw new AiInvalidResponseException("AI prompt could not be built");
        }

        AiResponse aiResponse = aiProvider.generate(new AiRequest(promptData.prompt(), TIMEOUT_SECONDS));
        List<String> suggestions = validateAndNormalizeSuggestions(aiResponse, promptData.existingCriteriaTexts());

        AiAcceptanceCriteriaDraftResponse response = new AiAcceptanceCriteriaDraftResponse();
        response.setTaskId(taskId);
        response.setSuggestions(suggestions);
        return response;
    }

    private PromptData loadPromptData(Long taskId) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        requireTaskAccess(task, currentUserService.getCurrentUser());
        validateTaskContext(task);

        List<AcceptanceCriteria> existingCriteria = acceptanceCriteriaRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId);
        String prompt = aiPromptBuilder.buildAcceptanceCriteriaPrompt(task, existingCriteria);
        List<String> existingCriteriaTexts = existingCriteria.stream()
                .map(AcceptanceCriteria::getText)
                .toList();
        return new PromptData(prompt, existingCriteriaTexts);
    }

    private List<String> validateAndNormalizeSuggestions(
            AiResponse aiResponse,
            List<String> existingCriteriaTexts
    ) {
        if (aiResponse == null || isBlank(aiResponse.getContent())) {
            throw new AiInvalidResponseException("AI response content is required");
        }

        List<String> suggestions;
        try {
            suggestions = objectMapper.readValue(aiResponse.getContent(), new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new AiInvalidResponseException("AI response must be a JSON array of strings");
        }

        if (suggestions == null) {
            throw new AiInvalidResponseException("AI response must be a JSON array of strings");
        }

        Set<String> existingNormalized = new LinkedHashSet<>();
        existingCriteriaTexts.stream()
                .map(this::normalizeForComparison)
                .forEach(existingNormalized::add);

        Set<String> seenSuggestions = new LinkedHashSet<>();
        List<String> normalizedSuggestions = suggestions.stream()
                .map(this::normalizeSuggestion)
                .filter(suggestion -> !existingNormalized.contains(normalizeForComparison(suggestion)))
                .filter(suggestion -> seenSuggestions.add(normalizeForComparison(suggestion)))
                .limit(MAX_SUGGESTIONS)
                .toList();

        if (normalizedSuggestions.size() < MIN_SUGGESTIONS) {
            throw new AiInvalidResponseException("AI response did not contain enough usable suggestions");
        }

        return normalizedSuggestions;
    }

    private String normalizeSuggestion(String suggestion) {
        if (isBlank(suggestion)) {
            throw new AiInvalidResponseException("AI suggestions must not be blank");
        }
        String normalized = suggestion.trim();
        if (normalized.length() > MAX_SUGGESTION_LENGTH) {
            throw new AiInvalidResponseException("AI suggestions cannot exceed 500 characters");
        }
        return normalized;
    }

    private void validateTaskContext(Task task) {
        boolean hasContext = !isBlank(task.getDescription())
                || !isBlank(task.getTechnicalContext())
                || !isBlank(task.getExpectedOutcome())
                || (task.getProject() != null && !isBlank(task.getProject().getName()));

        if (!hasContext) {
            throw new BadRequestException("Task needs description, technical context, expected outcome, or project context for AI suggestions");
        }
    }

    private String normalizeForComparison(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void requireTaskAccess(Task task, User currentUser) {
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (isAssignee(task, currentUser) || isProjectOwner(task.getProject(), currentUser)) {
            return;
        }
        throw new ForbiddenException("You can only access tasks assigned to you or owned through your projects");
    }

    private boolean isAssignee(Task task, User currentUser) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
    }

    private boolean isProjectOwner(Project project, User currentUser) {
        return project != null && project.getOwner() != null && project.getOwner().getId().equals(currentUser.getId());
    }

    private record PromptData(String prompt, List<String> existingCriteriaTexts) {
    }
}
