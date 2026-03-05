package com.jarvis.Model.Entity;
import com.github.javaparser.ast.CompilationUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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