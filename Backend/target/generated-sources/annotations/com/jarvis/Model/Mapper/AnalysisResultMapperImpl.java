package com.jarvis.Model.Mapper;

import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.DTO.CodeProblemDTO;
import com.jarvis.Model.Entity.AnalysisResult;
import com.jarvis.Model.Entity.CodeProblem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-08T22:48:23+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class AnalysisResultMapperImpl implements AnalysisResultMapper {

    @Override
    public AnalysisResultDTO toDTO(AnalysisResult analysisResult) {
        if ( analysisResult == null ) {
            return null;
        }

        AnalysisResultDTO.AnalysisResultDTOBuilder analysisResultDTO = AnalysisResultDTO.builder();

        analysisResultDTO.projectName( analysisResult.getProjectName() );
        analysisResultDTO.fileName( analysisResult.getFileName() );
        analysisResultDTO.packageName( analysisResult.getPackageName() );
        List<String> list = analysisResult.getClassNames();
        if ( list != null ) {
            analysisResultDTO.classNames( new ArrayList<String>( list ) );
        }
        analysisResultDTO.problems( codeProblemListToCodeProblemDTOList( analysisResult.getProblems() ) );
        analysisResultDTO.success( analysisResult.getSuccess() );
        analysisResultDTO.errorMessage( analysisResult.getErrorMessage() );
        analysisResultDTO.analysisTime( analysisResult.getAnalysisTime() );
        analysisResultDTO.problemCount( analysisResult.getProblemCount() );
        analysisResultDTO.user( analysisResult.getUser() );

        return analysisResultDTO.build();
    }

    @Override
    public AnalysisResult toEntity(AnalysisResultDTO analysisResultDTO) {
        if ( analysisResultDTO == null ) {
            return null;
        }

        AnalysisResult analysisResult = new AnalysisResult();

        analysisResult.setProjectName( analysisResultDTO.getProjectName() );
        analysisResult.setFileName( analysisResultDTO.getFileName() );
        analysisResult.setPackageName( analysisResultDTO.getPackageName() );
        List<String> list = analysisResultDTO.getClassNames();
        if ( list != null ) {
            analysisResult.setClassNames( new ArrayList<String>( list ) );
        }
        analysisResult.setProblems( codeProblemDTOListToCodeProblemList( analysisResultDTO.getProblems() ) );
        analysisResult.setSuccess( analysisResultDTO.getSuccess() );
        analysisResult.setErrorMessage( analysisResultDTO.getErrorMessage() );
        analysisResult.setAnalysisTime( analysisResultDTO.getAnalysisTime() );
        analysisResult.setProblemCount( analysisResultDTO.getProblemCount() );
        analysisResult.setUser( analysisResultDTO.getUser() );

        return analysisResult;
    }

    protected CodeProblemDTO codeProblemToCodeProblemDTO(CodeProblem codeProblem) {
        if ( codeProblem == null ) {
            return null;
        }

        CodeProblemDTO.CodeProblemDTOBuilder codeProblemDTO = CodeProblemDTO.builder();

        codeProblemDTO.fileName( codeProblem.getFileName() );
        codeProblemDTO.line( codeProblem.getLine() );
        codeProblemDTO.column( codeProblem.getColumn() );
        codeProblemDTO.message( codeProblem.getMessage() );
        codeProblemDTO.severity( codeProblem.getSeverity() );
        codeProblemDTO.createdAt( codeProblem.getCreatedAt() );
        codeProblemDTO.ruleId( codeProblem.getRuleId() );
        codeProblemDTO.snippet( codeProblem.getSnippet() );

        return codeProblemDTO.build();
    }

    protected List<CodeProblemDTO> codeProblemListToCodeProblemDTOList(List<CodeProblem> list) {
        if ( list == null ) {
            return null;
        }

        List<CodeProblemDTO> list1 = new ArrayList<CodeProblemDTO>( list.size() );
        for ( CodeProblem codeProblem : list ) {
            list1.add( codeProblemToCodeProblemDTO( codeProblem ) );
        }

        return list1;
    }

    protected CodeProblem codeProblemDTOToCodeProblem(CodeProblemDTO codeProblemDTO) {
        if ( codeProblemDTO == null ) {
            return null;
        }

        CodeProblem.CodeProblemBuilder codeProblem = CodeProblem.builder();

        codeProblem.fileName( codeProblemDTO.getFileName() );
        codeProblem.line( codeProblemDTO.getLine() );
        codeProblem.column( codeProblemDTO.getColumn() );
        codeProblem.message( codeProblemDTO.getMessage() );
        codeProblem.severity( codeProblemDTO.getSeverity() );
        codeProblem.createdAt( codeProblemDTO.getCreatedAt() );
        codeProblem.ruleId( codeProblemDTO.getRuleId() );
        codeProblem.snippet( codeProblemDTO.getSnippet() );

        return codeProblem.build();
    }

    protected List<CodeProblem> codeProblemDTOListToCodeProblemList(List<CodeProblemDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<CodeProblem> list1 = new ArrayList<CodeProblem>( list.size() );
        for ( CodeProblemDTO codeProblemDTO : list ) {
            list1.add( codeProblemDTOToCodeProblem( codeProblemDTO ) );
        }

        return list1;
    }
}
