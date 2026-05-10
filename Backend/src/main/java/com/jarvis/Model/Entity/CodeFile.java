package com.jarvis.Model.Entity;
import com.github.javaparser.ast.CompilationUnit;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeFile {
    private String fileName;
    private String filePath;
    private String sourceCode;
    private CompilationUnit ast;
    private List<CodeProblem> problems;
    private boolean isParsedSuccess;
    private String errorMessage;
}