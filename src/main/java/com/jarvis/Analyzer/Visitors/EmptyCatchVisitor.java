package com.jarvis.Analyzer.Visitors;

import com.jarvis.Analyzer.Core.BaseASTVisitor;
import com.jarvis.Model.Entity.CodeProblem;
import com.github.javaparser.ast.stmt.CatchClause;

public class EmptyCatchVisitor extends BaseASTVisitor {
    public EmptyCatchVisitor(String sourceCode, String fileName) {
        super(sourceCode, fileName);
    }

    @Override
    public void visit(CatchClause catchClause, Void arg){
        if(catchClause.getBody().getStatements().isEmpty()){
            CodeProblem codeProblem = createProblem(
                    catchClause,
                    "EMPTY_CATCH",
                    fileName,
                    "Пустой блок catch - ошибка может быть проигнорирована");
            problems.add(codeProblem);
        }
        super.visit(catchClause, arg);
    }
}