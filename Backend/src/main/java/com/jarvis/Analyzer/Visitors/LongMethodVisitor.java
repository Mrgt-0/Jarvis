package com.jarvis.Analyzer.Visitors;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.jarvis.Analyzer.Core.BaseASTVisitor;
import com.jarvis.Model.DTO.CodeProblemDTO;
import java.util.List;

public class LongMethodVisitor extends BaseASTVisitor {
    public LongMethodVisitor(String  sourceCode, String fileName) { super(sourceCode, fileName); }

    @Override
    public void visit(MethodDeclaration methodDeclaration, Void arg){
        List<CodeProblemDTO> problems  = getProblems();
        int lineCount = methodDeclaration.getEnd().get().line - methodDeclaration.getBegin().get().line;
        if(lineCount > 50){
            problems.add(createProblem(methodDeclaration,
                "LONG_METHOD",
                fileName,
                "Этот метод слишком длинный"));
        }
        super.visit(methodDeclaration, arg);
    }
}