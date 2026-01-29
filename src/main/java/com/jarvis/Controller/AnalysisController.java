package com.jarvis.Controller;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.DTO.ArchiveAnalysisRequestDTO;
import com.jarvis.Service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    @Operation(
            summary = "Анализировать Java файл",
            description = "Загрузите .java файл для статического анализа"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Анализ успешно выполнен",
                    content = @Content(
                            schema = @Schema(implementation = AnalysisResultDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат файла или ошибка анализа"
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Неподдерживаемый тип файла"
            )
    })
    @PostMapping(
            value = "/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalysisResultDTO> analyzeFile(
            @Parameter(
                    description = "Java файл для анализа",
                    required = true
            )
            @RequestParam("file") MultipartFile file,

            @Parameter(
                    description = "ID пользователя (опционально)",
                    required = false
            )
            @RequestParam(value = "userId", required = false, defaultValue = "anonymous") String userId,

            @Parameter(
                    description = "Название проекта (опционально)",
                    required = false
            )
            @RequestParam(value = "projectName", required = false, defaultValue = "default") String projectName) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(AnalysisResultDTO.error("Файл пустой", "Пустой файл"));
            }

            if (!file.getOriginalFilename().endsWith(".java")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(AnalysisResultDTO.error(file.getOriginalFilename(), "Только .java файлы поддерживаются"));
            }

            log.info("Анализ файла: {} (пользователь: {}, проект: {})",
                    file.getOriginalFilename(), userId, projectName);

            String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
            AnalysisResultDTO result = analysisService.analyzeJavaFile(sourceCode, file.getOriginalFilename());

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("Ошибка чтения файла: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(AnalysisResultDTO.error(file.getOriginalFilename(), "Ошибка чтения файла: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Ошибка анализа файла: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AnalysisResultDTO.error(file.getOriginalFilename(), "Внутренняя ошибка сервера"));
        }
    }

    @Operation(
            summary = "Анализировать ZIP архив",
            description = "Загрузите .zip архив с Java файлами для пакетного анализа"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Анализ успешно выполнен",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = AnalysisResultDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат архива или ошибка анализа"
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "Слишком большой файл"
            )
    })
    @PostMapping(
            value = "/archive",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AnalysisResultDTO>> analyzeArchive(
            @Parameter(
                    description = "ZIP архив с Java файлами",
                    required = true
            )
            @RequestParam("file") MultipartFile zipFile,

            @Parameter(
                    description = "Название проекта (опционально)",
                    required = false
            )
            @RequestParam(value = "projectName", required = false, defaultValue = "default") String projectName) {

        try {
            log.info("=== НАЧАЛО ОБРАБОТКИ ЗАПРОСА ===");
            log.info("Имя файла: {}", zipFile.getOriginalFilename());
            log.info("Размер файла: {} байт", zipFile.getSize());
            log.info("Проект: {}", projectName);

            if (zipFile.isEmpty()) {
                log.error("Файл пустой");
                return ResponseEntity.badRequest()
                        .body(List.of(AnalysisResultDTO.error("archive.zip", "Файл пустой")));
            }

            String filename = zipFile.getOriginalFilename();
            if (filename == null) {
                log.error("Имя файла null");
                return ResponseEntity.badRequest()
                        .body(List.of(AnalysisResultDTO.error("unknown.zip", "Имя файла отсутствует")));
            }

            byte[] zipData = zipFile.getBytes();
            log.info("Прочитано {} байт из архива", zipData.length);

            ArchiveAnalysisRequestDTO request = new ArchiveAnalysisRequestDTO();
            request.setProjectName(projectName);
            request.setArchiveName(filename);
            request.setZipData(zipData);

            log.info("Запрос создан, вызываем сервис...");
            List<AnalysisResultDTO> results = analysisService.analyzeZipArchive(request);
            log.info("Сервис вернул {} результатов", results.size());

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("=== КРИТИЧЕСКАЯ ОШИБКА ===");
            log.error("Сообщение: {}", e.getMessage());
            log.error("Класс исключения: {}", e.getClass().getName());
            log.error("Стек вызовов:", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(AnalysisResultDTO.error(
                            zipFile != null ? zipFile.getOriginalFilename() : "unknown.zip",
                            "Ошибка сервера: " + e.getClass().getSimpleName() + " - " + e.getMessage()
                    )));
        }
    }
}