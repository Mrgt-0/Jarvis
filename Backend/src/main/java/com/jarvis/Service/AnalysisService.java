package com.jarvis.Service;
import com.github.javaparser.ast.CompilationUnit;
import com.jarvis.Analyzer.Core.JavaCodeParser;
import com.jarvis.Analyzer.Core.RuleEngine;
import com.jarvis.Analyzer.PMD.PmdAnalyzer;
import com.jarvis.Model.DTO.*;
import com.jarvis.Model.Entity.CodeFile;
import com.jarvis.Model.Entity.User;
import com.jarvis.Model.Mapper.AnalysisResultMapper;
import com.jarvis.Repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalysisService {
    private final AnalysisResultRepository resultRepository;
    private final AnalysisStorageService storageService;
    private final JavaCodeParser javaCodeParser;
    private final RuleEngine ruleEngine;
    private final PmdAnalyzer pmdAnalyzer;
    private final AnalysisResultMapper analysisResultMapper;

    public void saveAnalysisResult(AnalysisResultDTO dto) {
        storageService.saveAnalysisResult(dto);
    }

    public List<AnalysisResultDTO> getAnalysisHistory(Long userId) {
        if(resultRepository.findByUserId(userId).isEmpty()) return Collections.emptyList();
        return resultRepository.findByUserId(userId).stream().map(analysisResultMapper::toDTO).collect(Collectors.toList());
    }
//анализ файлов
    public AnalysisResultDTO analyzeJavaFile(String sourceCode, String fileName, User user) {
        log.info("Анализ файла: {}", fileName);

        try {
            AnalysisRequestDTO analysisRequestDTO = new AnalysisRequestDTO(sourceCode, fileName);
            AnalysisResultDTO result = analyze(analysisRequestDTO);
            result.setUser(user);
            return result;
        } catch (Exception e) {
            log.error("Ошибка анализа файла {}: {}", fileName, e.getMessage());
            return AnalysisResultDTO.error(fileName, e.getMessage());
        }
    }

    public List<AnalysisResultDTO> analyzeZipArchive(ArchiveAnalysisRequestDTO request) {
        return analyzeArchive(request);
    }

    private AnalysisResultDTO analyze(AnalysisRequestDTO request) {
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
        List<CodeProblemDTO> problems = new ArrayList<>();
        problems.addAll(ruleEngine.analyze(ast, request.getSourceCode(), request.getFileName()));
        problems.addAll(pmdAnalyzer.analyzeFile(request.getSourceCode(), request.getFileName()));

        return AnalysisResultDTO.builder()
                .fileName(request.getFileName())
                .packageName(javaCodeParser.extractPackageName(ast).orElse(null))
                .classNames(javaCodeParser.extractClassNames(ast))
                .problems(problems)
                .success(true)
                .analysisTime(LocalDateTime.now())
                .build();
    }

    private AnalysisResultDTO createErrorResult(AnalysisRequestDTO request) {
        return AnalysisResultDTO.builder()
                .fileName(request.getFileName())
                .success(false)
                .errorMessage("Ошибка парсинга файла")
                .analysisTime(LocalDateTime.now())
                .build();
    }
//анализ архивов
    public List<AnalysisResultDTO> analyzeArchive(ArchiveAnalysisRequestDTO request) {
        if (request.getUser() == null)
            throw new IllegalArgumentException("Пользователь не может быть null при анализе архива");

        log.info("Начинаем анализ архива {} для пользователя {}",
                request.getArchiveName(), request.getUser().getUsername());

        List<CodeFile> files = extractJavaFilesFromZip(
                request.getZipData(),
                request.getArchiveName()
        );
        log.info("Извлечено {} Java файлов из архива {}", files.size(), request.getArchiveName());

        List<AnalysisResultDTO> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < files.size(); i++) {
            CodeFile file = files.get(i);
            log.info("[{}/{}] Анализ файла: {}", i + 1, files.size(), file.getFileName());

            try {
                AnalysisResultDTO result = analyzeFileInArchive(file, request);
                results.add(result);

                if (Boolean.TRUE.equals(result.getSuccess())) {
                    successCount++;
                    log.info("[{}/{}] Файл {} успешно проанализирован",
                            i + 1, files.size(), file.getFileName());
                } else {
                    errorCount++;
                    log.warn("[{}/{}] Файл {} проанализирован с ошибкой: {}",
                            i + 1, files.size(), file.getFileName(), result.getErrorMessage());
                }

            } catch (Exception e) {
                errorCount++;
                log.error("[{}/{}] Критическая ошибка при анализе файла {}: {}",
                        i + 1, files.size(), file.getFileName(), e.getMessage());

                AnalysisResultDTO errorResult = AnalysisResultDTO.builder()
                        .fileName(file.getFileName())
                        .projectName(request.getProjectName())
                        .user(request.getUser()) // Устанавливаем пользователя
                        .success(false)
                        .errorMessage("Критическая ошибка: " + e.getClass().getSimpleName())
                        .classNames(Collections.emptyList())
                        .problems(Collections.emptyList())
                        .build();
                results.add(errorResult);
            }
        }
        log.info("Анализ архива завершен. Успешно: {}, с ошибками: {}, всего: {}",
                successCount, errorCount, results.size());

        return results;
    }

    private AnalysisResultDTO analyzeFileInArchive(CodeFile file, ArchiveAnalysisRequestDTO request) {
        try {
            log.debug("Анализ файла из архива: {}", file.getFileName());

            if (file.getSourceCode() == null || file.getSourceCode().trim().isEmpty()) {
                log.warn("Файл {} пустой", file.getFileName());
                AnalysisResultDTO errorResult = AnalysisResultDTO.builder()
                        .fileName(file.getFileName())
                        .projectName(request.getProjectName())
                        .user(request.getUser())
                        .success(false)
                        .errorMessage("Файл пустой")
                        .classNames(Collections.emptyList())
                        .problems(Collections.emptyList())
                        .build();
                return errorResult;
            }

            AnalysisRequestDTO fileRequest = new AnalysisRequestDTO(
                    file.getSourceCode(),
                    file.getFileName()
            );
            AnalysisResultDTO result = analyze(fileRequest);
            result.setProjectName(request.getProjectName());
            result.setUser(request.getUser());

            if (result.getClassNames() == null)
                result.setClassNames(new ArrayList<>());
            if (result.getProblems() == null)
                result.setProblems(new ArrayList<>());

            log.info("Файл {} проанализирован. Успешно: {}, проблем: {}",
                    file.getFileName(), result.getSuccess(), result.getProblems().size());
            return result;

        } catch (Exception e) {
            log.error("Ошибка анализа файла {} из архива: {}", file.getFileName(), e.getMessage(), e);

            AnalysisResultDTO errorResult = AnalysisResultDTO.builder()
                    .fileName(file.getFileName())
                    .projectName(request.getProjectName())
                    .user(request.getUser()) // Устанавливаем пользователя
                    .success(false)
                    .errorMessage("Ошибка анализа: " + e.getClass().getSimpleName() + " - " + e.getMessage())
                    .classNames(Collections.emptyList())
                    .problems(Collections.emptyList())
                    .build();
            return errorResult;
        }
    }

    private List<CodeFile> extractJavaFilesFromZip(byte[] zipData, String archiveName) {
        List<CodeFile> javaFiles = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();

                if (!entry.isDirectory() && fileName.toLowerCase().endsWith(".java")) {
                    byte[] fileBytes = zis.readAllBytes();

                    if (fileBytes.length == 0) {
                        log.warn("Файл {} пустой, пропускаем", fileName);
                        continue;
                    }
                    String cleanedContent = cleanJavaFileContent(fileBytes, fileName);

                    CodeFile codeFile = new CodeFile();
                    codeFile.setFileName(fileName);
                    codeFile.setSourceCode(cleanedContent);
                    javaFiles.add(codeFile);
                    log.debug("Извлечен Java файл из архива: {} ({} байт)", fileName, fileBytes.length);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            log.error("Ошибка при чтении ZIP архива {}: {}", archiveName, e.getMessage());
        }
        log.info("Из архива {} извлечено {} Java файлов", archiveName, javaFiles.size());
        return javaFiles;
    }

    private String cleanJavaFileContent(byte[] fileBytes, String fileName) {
        try {
            String content = detectEncodingAndConvert(fileBytes);
            content = removeProblematicCharacters(content);
            if (!isLikelyValidJava(content))
                log.warn("Файл {} не похож на валидный Java код", fileName);
            return content;

        } catch (Exception e) {
            log.error("Ошибка очистки файла {}: {}", fileName, e.getMessage());
            return new String(fileBytes, StandardCharsets.UTF_8)
                    .replace("\u0000", "")
                    .replace("\uFEFF", "");
        }
    }

    private String detectEncodingAndConvert(byte[] bytes) {
        try {
            String utf8 = new String(bytes, StandardCharsets.UTF_8);
            if (!utf8.contains("\uFFFD"))
                return utf8;
        } catch (Exception ignored) {}
        try {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        } catch (Exception ignored) {}
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String removeProblematicCharacters(String content) {
        if (content == null) return "";
        content = content.replace("\uFEFF", "");
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t')
                cleaned.append(c);
            else if (c >= 32 && c <= 126)
                cleaned.append(c);
            else if (c >= 1040 && c <= 1103)
                cleaned.append(c);
            else if (c == 1025 || c == 1105)
                cleaned.append(c);
            else if (c == '\uFFFD')
                cleaned.append(' ');
            else {
                if (Character.isLetterOrDigit(c) || Character.isWhitespace(c))
                    cleaned.append(c);
                else
                    cleaned.append(' ');
            }
        }
        return cleaned.toString();
    }

    private boolean isLikelyValidJava(String content) {
        if (content == null || content.trim().isEmpty())
            return false;

        String trimmed = content.trim();
        return trimmed.contains("class ") ||
                trimmed.contains("public ") ||
                trimmed.contains("private ") ||
                trimmed.contains("protected ") ||
                trimmed.contains("import ") ||
                trimmed.contains("package ");
    }
}