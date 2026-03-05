package com.jarvis.Analyzer.Visitors;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.jarvis.Analyzer.Core.BaseASTVisitor;

public class SystemErrPrintVisitor extends BaseASTVisitor{
    public SystemErrPrintVisitor(String sourceCode, String fileName) { super(sourceCode,fileName); }

    @Override
    public void visit(MethodCallExpr methodCall, Void arg) {
        super.visit(methodCall,arg);
        String callText = methodCall.toString();

        if (callText.equals("System.err") || callText.startsWith("System.err(")) {
            getProblems().add(createProblem(
                    methodCall,
                    "SYSTEM_ERR",
                    fileName,
                    "Этот метод содержит System err"
            ));
        }
    }
}