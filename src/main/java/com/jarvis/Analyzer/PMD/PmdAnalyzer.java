package com.jarvis.Analyzer.PMD;
import com.jarvis.Model.DTO.CodeProblemDTO;
import com.jarvis.Model.Enum.ProblemSeverity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.rule.*;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                problems.add(convertViolation(violation, fileName));

            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
            log.debug("PMD анализ файла {}: найдено {} проблем", fileName, problems.size());

        } catch (Exception e) {
            log.error("Ошибка PMD анализа: {}", e.getMessage());
        }
        return problems;
    }

    private CodeProblemDTO convertViolation(RuleViolation violation, String fileName) {
        return CodeProblemDTO.builder()
                .ruleId("PMD_" + violation.getRule().getName())
                .fileName(fileName)
                .line(violation.getBeginLine())
                .column(violation.getBeginColumn())
                .message(violation.getDescription())
                .severity(mapSeverity(violation.getRule().getPriority()).toString()) //toString
                .build();
    }

    private ProblemSeverity mapSeverity(RulePriority priority) {
        if (priority.getPriority() <= 2) return ProblemSeverity.HIGH;
        if (priority.getPriority() == 3) return ProblemSeverity.MEDIUM;
        return ProblemSeverity.LOW;
    }
}