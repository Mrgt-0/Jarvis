package com.jarvis.Model.Mapper;
import com.jarvis.Model.DTO.CodeProblemDTO;
import com.jarvis.Model.Entity.CodeProblem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CodeProblemMapper {
    CodeProblem toEntity(CodeProblemDTO dto);
    CodeProblemDTO toDTO(CodeProblem entity);
}