package com.jarvis.Analyzer.Visitors;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.jarvis.Analyzer.Core.BaseASTVisitor;
import com.jarvis.Model.DTO.CodeProblemDTO;
import java.util.List;

public class SystemOutPrintVisitor extends BaseASTVisitor {
    public SystemOutPrintVisitor(String  sourceCode, String fileName) { super(sourceCode, fileName); }

    @Override
    public void visit(MethodCallExpr methodCall, Void ard){
        super.visit(methodCall, ard);
        List<CodeProblemDTO> problems = getProblems();
        String methodName = methodCall.getNameAsString();

        if (!methodName.equals("print") &&
                !methodName.equals("println") &&
                !methodName.equals("printf")) {
            return;
        }

        String callText = methodCall.toString();
        if (callText.startsWith("System.out")) {
            problems.add(createProblem(methodCall,
                    "SYSTEM_OUT",
                    fileName,
                    "Этот метод содержит System out"));
        }
    }
}