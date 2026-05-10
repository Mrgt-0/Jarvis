package com.jarvis.Controller;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.DTO.ArchiveAnalysisRequestDTO;
import com.jarvis.Model.Entity.User;
import com.jarvis.Service.AnalysisService;
import com.jarvis.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;
    private final UserService userService;

    @PostMapping(value = "/saveAnalysisResult")
    public ResponseEntity<?>  saveAnalysisResult(@RequestBody AnalysisResultDTO dto) {
        analysisService.saveAnalysisResult(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/history")
    public ResponseEntity<List<AnalysisResultDTO>> getAnalysisHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(analysisService.getAnalysisHistory(userId));
    }

    @DeleteMapping(value = "/deleteAnalysisResult/{id}")
    public ResponseEntity<?> deleteAnalysisResult(@PathVariable("id") Long id) {
        analysisService.deleteAnalysisResult(id);
        log.info("Analysis result {} deleted successfully", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "projectName", required = false, defaultValue = "default") String projectName,
            HttpSession session
    ) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                log.warn("Доступ запрещен: пользователь не аутентифицирован");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "Требуется аутентификация",
                                "message", "Пожалуйста, войдите в систему перед анализом файлов"
                        ));
            }
            User user;
            try {
                user = userService.getUserByUserId(userId);
                log.info("Пользователь аутентифицирован: {} (ID: {})", user.getUsername(), userId);
            } catch (Exception e) {
                log.error("Пользователь с ID {} не найден в БД: {}", userId, e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Пользователь не найден"));
            }
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Файл пустой"));
            }
            if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".java")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(Map.of("error", "Только .java файлы поддерживаются"));
            }
            String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
            AnalysisResultDTO result = analysisService.analyzeJavaFile(sourceCode, file.getOriginalFilename(), user);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Требуется аутентификация"));
        } catch (IOException e) {
            log.error("Ошибка чтения файла: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка чтения файла"));
        } catch (Exception e) {
            log.error("Ошибка анализа: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Внутренняя ошибка сервера"));
        }
    }

    @Operation(summary = "Анализировать ZIP архив")
    @PostMapping(value = "/archive", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeArchive(
            @Parameter(description = "ZIP архив с Java файлами", required = true)
            @RequestParam("file") MultipartFile zipFile,
            @Parameter(description = "Название проекта", required = false)
            @RequestParam(value = "projectName", required = false, defaultValue = "default") String projectName,
            HttpSession session) {

        log.info("=== НАЧАЛО АНАЛИЗА АРХИВА ===");

        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                log.warn("Доступ запрещен: пользователь не аутентифицирован");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Требуется аутентификация"));
            }
            User user;
            try {
                user = userService.getUserByUserId(userId);
                log.info("Пользователь аутентифицирован: {} (ID: {})", user.getUsername(), userId);
            } catch (Exception e) {
                log.error("Пользователь с ID {} не найден: {}", userId, e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Пользователь не найден"));
            }
            if (zipFile.isEmpty()) {
                log.warn("Передан пустой файл");
                return ResponseEntity.badRequest()
                        .body("Файл пустой");
            }

            String filename = zipFile.getOriginalFilename();
            if (filename == null) {
                log.warn("Имя файла отсутствует");
                return ResponseEntity.badRequest()
                        .body("Имя файла отсутствует");
            }

            if (!filename.toLowerCase().endsWith(".zip")) {
                log.warn("Неподдерживаемый формат файла: {}", filename);
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("Только ZIP архивы поддерживаются");
            }
            log.info("Начало обработки архива: {} (размер: {} байт)", filename, zipFile.getSize());
            byte[] zipData = zipFile.getBytes();
            log.info("Прочитано {} байт из архива", zipData.length);

            ArchiveAnalysisRequestDTO request = new ArchiveAnalysisRequestDTO();
            request.setProjectName(projectName);
            request.setArchiveName(filename);
            request.setZipData(zipData);
            request.setUser(user);
            log.info("Вызов сервиса анализа архива...");
            List<AnalysisResultDTO> results = analysisService.analyzeZipArchive(request);
            log.info("Сервис вернул {} результатов", results.size());
            return ResponseEntity.ok(results);

        } catch (IOException e) {
            log.error("Ошибка чтения файла: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Ошибка чтения файла");
        } catch (RuntimeException e) {
            log.error("Ошибка аутентификации: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Требуется аутентификация"));
        } catch (Exception e) {
            log.error("Критическая ошибка при анализе архива: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Критическая ошибка сервера");
        }
    }
}