package com.jarvis.Model.Mapper;

import com.jarvis.Model.DTO.CodeProblemDTO;
import com.jarvis.Model.Entity.CodeProblem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-08T22:48:23+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class CodeProblemMapperImpl implements CodeProblemMapper {

    @Override
    public CodeProblem toEntity(CodeProblemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        CodeProblem.CodeProblemBuilder codeProblem = CodeProblem.builder();

        codeProblem.fileName( dto.getFileName() );
        codeProblem.line( dto.getLine() );
        codeProblem.column( dto.getColumn() );
        codeProblem.message( dto.getMessage() );
        codeProblem.severity( dto.getSeverity() );
        codeProblem.createdAt( dto.getCreatedAt() );
        codeProblem.ruleId( dto.getRuleId() );
        codeProblem.snippet( dto.getSnippet() );

        return codeProblem.build();
    }

    @Override
    public CodeProblemDTO toDTO(CodeProblem entity) {
        if ( entity == null ) {
            return null;
        }

        CodeProblemDTO.CodeProblemDTOBuilder codeProblemDTO = CodeProblemDTO.builder();

        codeProblemDTO.fileName( entity.getFileName() );
        codeProblemDTO.line( entity.getLine() );
        codeProblemDTO.column( entity.getColumn() );
        codeProblemDTO.message( entity.getMessage() );
        codeProblemDTO.severity( entity.getSeverity() );
        codeProblemDTO.createdAt( entity.getCreatedAt() );
        codeProblemDTO.ruleId( entity.getRuleId() );
        codeProblemDTO.snippet( entity.getSnippet() );

        return codeProblemDTO.build();
    }
}
