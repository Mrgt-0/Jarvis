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
import java.util.*;

@Slf4j
@Service
public class PmdAnalyzer {
    private PMDConfiguration config;
    private RuleSet rules;

    private static final Map<String, String> RUSSIAN_MESSAGES = new HashMap<>();
    private static final Map<String, String> RUSSIAN_RULE_NAMES = new HashMap<>();

    static {
        RUSSIAN_MESSAGES.put("This utility class has a non-private constructor",
                "Этот вспомогательный класс имеет открытый конструктор");
        RUSSIAN_MESSAGES.put("Utility classes should not have public or default constructor",
                "Вспомогательные классы не должны иметь открытых конструкторов");
        RUSSIAN_MESSAGES.put("Avoid using System.out.println",
                "Избегайте использования System.out.println");
        RUSSIAN_MESSAGES.put("System.out.println is used",
                "Используется System.out.println");
        RUSSIAN_MESSAGES.put("Avoid using printStackTrace",
                "Избегайте использования printStackTrace");
        RUSSIAN_MESSAGES.put("Unused local variable",
                "Неиспользуемая локальная переменная");
        RUSSIAN_MESSAGES.put("Unused private field",
                "Неиспользуемое приватное поле");
        RUSSIAN_MESSAGES.put("Unused method parameter",
                "Неиспользуемый параметр метода");
        RUSSIAN_MESSAGES.put("Avoid unused imports",
                "Избегайте неиспользуемых импортов");
        RUSSIAN_MESSAGES.put("Method length is too long",
                "Метод слишком длинный");
        RUSSIAN_MESSAGES.put("Class has too many methods",
                "Класс содержит слишком много методов");
        RUSSIAN_MESSAGES.put("Avoid deeply nested if statements",
                "Избегайте глубоко вложенных if конструкций");
        RUSSIAN_MESSAGES.put("Avoid empty catch blocks",
                "Избегайте пустых catch блоков");
        RUSSIAN_MESSAGES.put("Avoid empty if statements",
                "Избегайте пустых if операторов");
        RUSSIAN_MESSAGES.put("Avoid empty try blocks",
                "Избегайте пустых try блоков");
        RUSSIAN_MESSAGES.put("Avoid reassigning parameters",
                "Избегайте переназначения параметров");
        RUSSIAN_MESSAGES.put("Avoid using null in conditional statements",
                "Избегайте использования null в условных операторах");
        RUSSIAN_MESSAGES.put("Use try-with-resources",
                "Используйте try-with-resources");
        RUSSIAN_MESSAGES.put("Close resources properly",
                "Закрывайте ресурсы правильно");
        RUSSIAN_MESSAGES.put("Class with only private constructors should be declared final",
                "Класс только с приватными конструкторами должен быть объявлен как final");
        RUSSIAN_MESSAGES.put("Avoid variables with short names",
                "Избегайте коротких имен переменных");
        RUSSIAN_MESSAGES.put("Avoid variables with long names",
                "Избегайте слишком длинных имен переменных");
        RUSSIAN_MESSAGES.put("Document empty method",
                "Документируйте пустой метод");
        RUSSIAN_MESSAGES.put("Avoid duplicate literals",
                "Избегайте дублирования литералов");
        RUSSIAN_MESSAGES.put("Avoid unused local variables",
                "Избегайте неиспользуемых локальных переменных");
        RUSSIAN_MESSAGES.put("Avoid unused private methods",
                "Избегайте неиспользуемых приватных методов");

        RUSSIAN_RULE_NAMES.put("UnusedLocalVariable", "Неиспользуемая локальная переменная");
        RUSSIAN_RULE_NAMES.put("UnusedPrivateField", "Неиспользуемое приватное поле");
        RUSSIAN_RULE_NAMES.put("UnusedFormalParameter", "Неиспользуемый параметр");
        RUSSIAN_RULE_NAMES.put("UnusedImports", "Неиспользуемые импорты");
        RUSSIAN_RULE_NAMES.put("SystemPrintln", "Использование System.out.println");
        RUSSIAN_RULE_NAMES.put("AvoidPrintStackTrace", "Избегайте printStackTrace");
        RUSSIAN_RULE_NAMES.put("TooManyMethods", "Слишком много методов");
        RUSSIAN_RULE_NAMES.put("ExcessiveMethodLength", "Метод слишком длинный");
        RUSSIAN_RULE_NAMES.put("NestedIfDepth", "Глубокая вложенность if");
        RUSSIAN_RULE_NAMES.put("EmptyCatchBlock", "Пустой catch блок");
        RUSSIAN_RULE_NAMES.put("EmptyIfStmt", "Пустой if оператор");
        RUSSIAN_RULE_NAMES.put("EmptyTryBlock", "Пустой try блок");
        RUSSIAN_RULE_NAMES.put("AssignmentToMethodParameter", "Присвоение параметру метода");
        RUSSIAN_RULE_NAMES.put("UseTryWithResources", "Используйте try-with-resources");
        RUSSIAN_RULE_NAMES.put("LoggerIsNotStaticFinal", "Логгер должен быть static final");
        RUSSIAN_RULE_NAMES.put("ClassWithOnlyPrivateConstructorsShouldBeFinal",
                "Класс с приватными конструкторами должен быть final");
        RUSSIAN_RULE_NAMES.put("AvoidDuplicateLiterals", "Избегайте дублирования литералов");
        RUSSIAN_RULE_NAMES.put("CommentRequired", "Требуется комментарий");
    }

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
        String originalMessage = violation.getDescription();
        String russianMessage = translateMessage(originalMessage);

        String originalRuleName = violation.getRule().getName();

        String snippet = extractSnippet(violation, sourceCode);

        return CodeProblemDTO.builder()
                .ruleId("PMD_" + originalRuleName)
                .fileName(fileName)
                .line(violation.getBeginLine())
                .column(violation.getBeginColumn())
                .message(russianMessage)
                .severity(mapSeverity(violation.getRule().getPriority()).toString())
                .snippet(snippet != null ? snippet : "")
                .build();
    }

    private String translateMessage(String englishMessage) {
        if (englishMessage == null) return "";
        return RUSSIAN_MESSAGES.getOrDefault(englishMessage, englishMessage);
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
                String line = lines[i - 1]; // Индексация с 0

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