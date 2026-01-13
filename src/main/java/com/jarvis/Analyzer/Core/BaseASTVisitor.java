package com.jarvis.Analyzer.Core;
import com.jarvis.Model.Entity.CodeProblem;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseASTVisitor extends VoidVisitorAdapter<Void> {
    protected final String sourceCode;
    protected final String fileName;
    protected final List<CodeProblem> problems = new ArrayList<>();

    protected BaseASTVisitor(String sourceCode, String fileName) {
        this.sourceCode = sourceCode;
        this.fileName = fileName;
    }

    protected List<CodeProblem> getProblems() {
        return problems;
    }

    protected CodeProblem createProblem(Node node, String ruleId, String fileName, String message) {
        int line = node.getRange().map(r -> r.begin.line).orElse(-1);
        int column = node.getRange().map(r -> r.begin.column).orElse(-1);
        String snippet = extractSnippet(node);

        return CodeProblem.of(
                ruleId, fileName,
                line, column, snippet,
                message
        );
    }

    protected String extractSnippet(Node node) {
        return node.getRange()
                .map(range -> {
                    String[] lines = sourceCode.split("\n");
                    int startLine = Math.max(0, range.begin.line - 2);
                    int endLine = Math.min(lines.length, range.end.line + 2);

                    StringBuilder snippet = new StringBuilder();
                    for (int i = startLine; i <= endLine; i++)
                        snippet.append(lines[i]).append("\n");
                    return snippet.toString();
                })
                .orElse("");
    }
}