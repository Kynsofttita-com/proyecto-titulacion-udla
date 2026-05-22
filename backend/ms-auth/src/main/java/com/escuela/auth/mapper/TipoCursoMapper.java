package com.escuela.auth.mapper;

import com.escuela.auth.dto.TipoCursoResponse;
import com.escuela.auth.entity.CategoriaLicencia;
import com.escuela.auth.entity.TipoCurso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TipoCursoMapper {

    @Mapping(target = "categoriaLicenciaId",
            source = "categoriaLicencia",
            qualifiedByName = "catId")
    @Mapping(target = "categoriaLicenciaCodigo",
            source = "categoriaLicencia",
            qualifiedByName = "catCodigo")
    TipoCursoResponse toResponse(TipoCurso entity);

    @Named("catId")
    default Long catId(CategoriaLicencia cat) {
        return cat == null ? null : cat.getId();
    }

    @Named("catCodigo")
    default String catCodigo(CategoriaLicencia cat) {
        return cat == null ? null : cat.getCodigo();
    }
}
