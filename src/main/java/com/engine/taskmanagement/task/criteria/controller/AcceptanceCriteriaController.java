package com.engine.taskmanagement.task.criteria.controller;

import com.engine.taskmanagement.task.criteria.dto.request.BulkCreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.CreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.UpdateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.response.AcceptanceCriteriaResponse;
import com.engine.taskmanagement.task.criteria.service.abstraction.AcceptanceCriteriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/acceptance-criteria")
public class AcceptanceCriteriaController {

    private final AcceptanceCriteriaService acceptanceCriteriaService;

    public AcceptanceCriteriaController(AcceptanceCriteriaService acceptanceCriteriaService) {
        this.acceptanceCriteriaService = acceptanceCriteriaService;
    }

    @PostMapping
    public ResponseEntity<AcceptanceCriteriaResponse> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateAcceptanceCriteriaRequest request
    ) {
        AcceptanceCriteriaResponse response = acceptanceCriteriaService.create(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<AcceptanceCriteriaResponse>> bulkCreate(
            @PathVariable Long taskId,
            @Valid @RequestBody BulkCreateAcceptanceCriteriaRequest request
    ) {
        List<AcceptanceCriteriaResponse> response = acceptanceCriteriaService.bulkCreate(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AcceptanceCriteriaResponse>> list(@PathVariable Long taskId) {
        return ResponseEntity.ok(acceptanceCriteriaService.list(taskId));
    }

    @PatchMapping("/{criteriaId}")
    public ResponseEntity<AcceptanceCriteriaResponse> update(
            @PathVariable Long taskId,
            @PathVariable Long criteriaId,
            @Valid @RequestBody UpdateAcceptanceCriteriaRequest request
    ) {
        return ResponseEntity.ok(acceptanceCriteriaService.update(taskId, criteriaId, request));
    }

    @PatchMapping("/{criteriaId}/complete")
    public ResponseEntity<AcceptanceCriteriaResponse> complete(
            @PathVariable Long taskId,
            @PathVariable Long criteriaId
    ) {
        return ResponseEntity.ok(acceptanceCriteriaService.complete(taskId, criteriaId));
    }

    @PatchMapping("/{criteriaId}/reopen")
    public ResponseEntity<AcceptanceCriteriaResponse> reopen(
            @PathVariable Long taskId,
            @PathVariable Long criteriaId
    ) {
        return ResponseEntity.ok(acceptanceCriteriaService.reopen(taskId, criteriaId));
    }

    @DeleteMapping("/{criteriaId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long taskId,
            @PathVariable Long criteriaId
    ) {
        acceptanceCriteriaService.delete(taskId, criteriaId);
        return ResponseEntity.noContent().build();
    }
}
