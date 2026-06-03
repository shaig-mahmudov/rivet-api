package com.engine.taskmanagement.project.mapper;

import com.engine.taskmanagement.project.dto.request.CreateProjectRequest;
import com.engine.taskmanagement.project.dto.request.PartialUpdateProjectRequest;
import com.engine.taskmanagement.project.dto.request.UpdateProjectRequest;
import com.engine.taskmanagement.project.dto.response.ProjectResponse;
import com.engine.taskmanagement.project.entity.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Project toEntity(CreateProjectRequest request);

    ProjectResponse toResponse(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateProjectRequest request, @MappingTarget Project project);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void partialUpdateEntity(PartialUpdateProjectRequest request, @MappingTarget Project project);
}
