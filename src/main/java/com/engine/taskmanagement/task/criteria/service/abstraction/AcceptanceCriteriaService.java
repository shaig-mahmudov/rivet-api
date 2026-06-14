package com.engine.taskmanagement.task.criteria.service.abstraction;

import com.engine.taskmanagement.task.criteria.dto.request.BulkCreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.CreateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.request.UpdateAcceptanceCriteriaRequest;
import com.engine.taskmanagement.task.criteria.dto.response.AcceptanceCriteriaResponse;

import java.util.List;

public interface AcceptanceCriteriaService {

    AcceptanceCriteriaResponse create(Long taskId, CreateAcceptanceCriteriaRequest request);

    List<AcceptanceCriteriaResponse> bulkCreate(Long taskId, BulkCreateAcceptanceCriteriaRequest request);

    List<AcceptanceCriteriaResponse> list(Long taskId);

    AcceptanceCriteriaResponse update(Long taskId, Long criteriaId, UpdateAcceptanceCriteriaRequest request);

    AcceptanceCriteriaResponse complete(Long taskId, Long criteriaId);

    AcceptanceCriteriaResponse reopen(Long taskId, Long criteriaId);

    void delete(Long taskId, Long criteriaId);
}
