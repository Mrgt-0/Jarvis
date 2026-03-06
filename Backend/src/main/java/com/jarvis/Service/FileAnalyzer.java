package com.jarvis.Service;
import com.github.javaparser.ast.CompilationUnit;
import com.jarvis.Analyzer.Core.JavaCodeParser;
import com.jarvis.Analyzer.Core.RuleEngine;
import com.jarvis.Analyzer.PMD.PmdAnalyzer;
import com.jarvis.Model.DTO.AnalysisRequestDTO;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.DTO.CodeProblemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAnalyzer {
    private final JavaCodeParser javaCodeParser;
    private final RuleEngine ruleEngine;
    private final PmdAnalyzer pmdAnalyzer;

    public AnalysisResultDTO analyze(AnalysisRequestDTO request) {
        try{
            return javaCodeParser.parseFile(request.getSourceCode(), request.getFileName())
                    .map(ast -> createAnalysisResult(ast, request))
                    .orElseGet(() -> createErrorResult(request));
        }catch (AssertionError e) {
            log.error("Ошибка парсинга файла {}: {}", request.getFileName(), e.getMessage());
            return createErrorResult(request);
        } catch (Exception e) {
            log.error("Общая ошибка анализа файла {}: {}", request.getFileName(), e.getMessage());
            return createErrorResult(request);
        }
    }

    private AnalysisResultDTO createAnalysisResult(CompilationUnit ast, AnalysisRequestDTO request) {
        List<CodeProblemDTO> problems = detectProblems(ast, request);

        return AnalysisResultDTO.builder()
                .fileName(request.getFileName())
                .packageName(javaCodeParser.extractPackageName(ast).orElse(null))
                .classNames(javaCodeParser.extractClassNames(ast))
                .problems(problems)
                .success(true)
                .analysisTime(LocalDateTime.now())
                .build();
    }

    private List<CodeProblemDTO> detectProblems(CompilationUnit ast, AnalysisRequestDTO request) {
        List<CodeProblemDTO> problems = new ArrayList<>();
        problems.addAll(ruleEngine.analyze(ast, request.getSourceCode(), request.getFileName()));
        problems.addAll(pmdAnalyzer.analyzeFile(request.getSourceCode(), request.getFileName()));
        return problems;
    }

    private AnalysisResultDTO createErrorResult(AnalysisRequestDTO request) {
        return AnalysisResultDTO.builder()
                .fileName(request.getFileName())
                .success(false)
                .errorMessage("Ошибка парсинга файла")
                .analysisTime(LocalDateTime.now())
                .build();
    }
}