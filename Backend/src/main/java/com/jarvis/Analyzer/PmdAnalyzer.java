package com.jarvis.Analyzer;
import com.jarvis.Model.DTO.*;
import com.jarvis.Model.Enum.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.rule.*;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.util.*;

@Slf4j
@Service
public class PmdAnalyzer {
    private PMDConfiguration config;
    private RuleSet rules;

    @PostConstruct
    public void init() {
        config = new PMDConfiguration();
        config.setDefaultLanguageVersion(JavaLanguageModule.getInstance().getDefaultVersion());
        config.setIgnoreIncrementalAnalysis(true);

        try {
            RuleSetLoader loader = RuleSetLoader.fromPmdConfig(config);
            rules = loader.loadFromResource("rulesets/java/quickstart.xml");
            log.info("Загружено {} правил PMD", rules.getRules().size());
        } catch (Exception e) {
            log.error("Ошибка загрузки правил PMD: {}", e.getMessage());
            rules = null;
        }
    }

    public List<CodeProblemDTO> analyzeFile(String sourceCode, String fileName) {
        if (rules == null)
            return Collections.emptyList();
        List<CodeProblemDTO> problems = new ArrayList<>();

        try {
            Path tempDir = Files.createTempDirectory("pmd_analysis_");
            Path tempFile = tempDir.resolve(fileName);
            Files.createDirectories(tempFile.getParent());
            Files.writeString(tempFile, sourceCode);

            PmdAnalysis pmd = PmdAnalysis.create(config);
            pmd.addRuleSet(rules);
            pmd.files().addFile(tempFile);

            SimpleCollector renderer = new SimpleCollector();
            pmd.addRenderer(renderer);
            pmd.performAnalysis();

            for (RuleViolation violation : renderer.getViolations())
                problems.add(convertViolation(violation, fileName, sourceCode));

            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
            log.debug("PMD анализ файла {}: найдено {} проблем", fileName, problems.size());

        } catch (Exception e) {
            log.error("Ошибка PMD анализа: {}", e.getMessage());
        }
        return problems;
    }

    private CodeProblemDTO convertViolation(RuleViolation violation, String fileName, String sourceCode) {
        String snippet = extractSnippet(violation, sourceCode);
        return CodeProblemDTO.builder()
                .ruleId("PMD_" + violation.getRule().getName())
                .fileName(fileName)
                .line(violation.getBeginLine())
                .column(violation.getBeginColumn())
                .message(violation.getDescription())
                .severity(mapSeverity(violation.getRule().getPriority()).toString())
                .snippet(snippet != null ? snippet : "")
                .build();
    }

    private ProblemSeverity mapSeverity(RulePriority priority) {
        if (priority.getPriority() <= 2) return ProblemSeverity.HIGH;
        if (priority.getPriority() == 3) return ProblemSeverity.MEDIUM;
        return ProblemSeverity.LOW;
    }

    private String extractSnippet(RuleViolation violation, String sourceCode) {
        if (sourceCode == null || sourceCode.isEmpty())
            return "";
        try {
            String[] lines = sourceCode.split("\n", -1);

            int startLine = violation.getBeginLine();
            int endLine = violation.getEndLine();
            if (endLine < startLine)
                endLine = startLine;

            int contextStart = Math.max(1, startLine - 2);
            int contextEnd = Math.min(lines.length, endLine + 2);
            StringBuilder snippet = new StringBuilder();

            for (int i = contextStart; i <= contextEnd; i++) {
                String lineNumber = String.format("%4d | ", i);
                String line = lines[i - 1];

                if (i >= startLine && i <= endLine) {
                    snippet.append("⚠️ ").append(lineNumber).append(line).append("\n");

                    if (i == startLine && violation.getBeginColumn() > 0) {
                        int column = violation.getBeginColumn();
                        StringBuilder pointer = new StringBuilder("    ");
                        for (int j = 0; j < column + 5; j++)
                            pointer.append(" ");

                        pointer.append("↑ здесь");
                        snippet.append(pointer).append("\n");
                    }
                } else
                    snippet.append("   ").append(lineNumber).append(line).append("\n");
            }
            return snippet.toString();
        } catch (Exception e) {
            log.error("Ошибка при извлечении фрагмента кода: {}", e.getMessage());
            return "Не удалось извлечь фрагмент кода";
        }
    }
}