package com.jarvis.Analyzer.Core;
import com.jarvis.Analyzer.Visitors.EmptyCatchVisitor;
import com.jarvis.Analyzer.Visitors.LongMethodVisitor;
import com.jarvis.Analyzer.Visitors.SystemErrPrintVisitor;
import com.jarvis.Analyzer.Visitors.SystemOutPrintVisitor;
import com.jarvis.Model.Entity.CodeProblem;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RuleEngine {
    private final List<Class<? extends BaseASTVisitor>> ruleVisitors;
    public RuleEngine() {
        this.ruleVisitors = List.of(
                EmptyCatchVisitor.class,
                LongMethodVisitor.class,
                SystemOutPrintVisitor.class,
                SystemErrPrintVisitor.class
        );
    }

    public List<CodeProblem> analyze(CompilationUnit cu, String sourceCode, String fileName) {
        List<CodeProblem> problems = new ArrayList<>();
        for (Class<? extends BaseASTVisitor> visitorClass : ruleVisitors) {
            try{
                BaseASTVisitor visitor = visitorClass
                        .getConstructor(String.class, String.class)
                        .newInstance(sourceCode, fileName);

                cu.accept(visitor, null);
                problems.addAll(visitor.getProblems());

                log.debug("Применено правило: {} найдено  {} проблем.",
                        visitor.getClass().getSimpleName(), visitor.getProblems().size());
            } catch (Exception e){
                log.error("Ошибка при применении правила {}: {}",
                        visitorClass.getSimpleName(), e.getMessage());
            }
        }
        log.info("Анализ файла {} завершен: найдено {} проблем",
                fileName, problems.size());

        return problems;
    }
}
