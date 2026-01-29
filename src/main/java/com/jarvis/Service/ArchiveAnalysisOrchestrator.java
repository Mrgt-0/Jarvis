package com.jarvis.Service;
import com.jarvis.Model.DTO.AnalysisRequestDTO;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.DTO.ArchiveAnalysisRequestDTO;
import com.jarvis.Model.Entity.CodeFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArchiveAnalysisOrchestrator {
    private final FileAnalyzer fileAnalyzer;
    private final ArchiveProcessingService archiveProcessingService;
    private final AnalysisStorageService storageService;

    public List<AnalysisResultDTO> analyzeArchive(ArchiveAnalysisRequestDTO request) {
        List<CodeFile> files = archiveProcessingService.extractJavaFilesFromZip(
                request.getZipData(),
                request.getArchiveName()
        );

        log.info("Начинаем анализ {} файлов из архива {}", files.size(), request.getArchiveName());

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
                        .success(false)
                        .errorMessage("Критическая ошибка: " + e.getClass().getSimpleName())
                        .classNames(Collections.emptyList())
                        .problems(Collections.emptyList())
                        .build();

                results.add(errorResult);
                storageService.saveAnalysisResult(errorResult);
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
                        .success(false)
                        .errorMessage("Файл пустой")
                        .classNames(Collections.emptyList())
                        .problems(Collections.emptyList())
                        .build();

                storageService.saveAnalysisResult(errorResult);
                return errorResult;
            }

            AnalysisRequestDTO fileRequest = new AnalysisRequestDTO(
                    file.getSourceCode(),
                    file.getFileName()
            );
            AnalysisResultDTO result = fileAnalyzer.analyze(fileRequest);
            result.setProjectName(request.getProjectName());

            if (result.getClassNames() == null)
                result.setClassNames(new ArrayList<>());
            if (result.getProblems() == null)
                result.setProblems(new ArrayList<>());

            log.info("Файл {} проанализирован. Успешно: {}, проблем: {}",
                    file.getFileName(), result.getSuccess(), result.getProblems().size());
            storageService.saveAnalysisResult(result);

            return result;

        } catch (Exception e) {
            log.error("Ошибка анализа файла {} из архива: {}", file.getFileName(), e.getMessage(), e);

            AnalysisResultDTO errorResult = AnalysisResultDTO.builder()
                    .fileName(file.getFileName())
                    .projectName(request.getProjectName())
                    .success(false)
                    .errorMessage("Ошибка анализа: " + e.getClass().getSimpleName() + " - " + e.getMessage())
                    .classNames(Collections.emptyList())
                    .problems(Collections.emptyList())
                    .build();

            storageService.saveAnalysisResult(errorResult);
            return errorResult;
        }
    }
}