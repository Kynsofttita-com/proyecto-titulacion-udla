package com.escuela.instructores.mapper;

import com.escuela.instructores.dto.CreateInstructorRequest;
import com.escuela.instructores.dto.InstructorListResponse;
import com.escuela.instructores.dto.InstructorResponse;
import com.escuela.instructores.entity.Instructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InstructorMapper {

    InstructorResponse toResponse(Instructor entity);

    InstructorListResponse toListResponse(Instructor entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    Instructor toEntity(CreateInstructorRequest request);
}
