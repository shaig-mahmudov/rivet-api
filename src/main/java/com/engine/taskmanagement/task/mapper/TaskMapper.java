package com.engine.taskmanagement.task.mapper;

import com.engine.taskmanagement.task.dto.request.*;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.entity.Task;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "priority", source = "priority", defaultValue = "MEDIUM")
    @Mapping(target = "status", source = "status", defaultValue = "TODO")
    Task toEntity(CreateTaskRequest request);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    TaskResponse toResponse(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(UpdateTaskRequest request, @MappingTarget Task task);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void partialUpdateEntity(PartialUpdateTaskRequest request, @MappingTarget Task task);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "status", source = "status")
    void changeTaskStatus(ChangeTaskStatusRequest request, @MappingTarget Task task);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "priority", source = "priority")
    void changeTaskPriority(ChangeTaskPriorityRequest request, @MappingTarget Task task);
}
