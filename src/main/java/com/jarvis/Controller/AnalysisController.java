package com.jarvis.Controller;
import com.jarvis.Model.Entity.AnalysisResult;
import com.jarvis.Service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    @Operation(
            summary = "Анализировать Java файл",
            description = "Загрузите .java файл для анализа"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Анализ успешно выполнен",
                    content = @Content(
                            schema = @Schema(implementation = AnalysisResult.class)
                    )
            )
    })
    @PostMapping(
            value = "/analyze-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalysisResult> analyzeFile(
            @Parameter(
                    description = "Java файл для анализа",
                    required = true
            )
            @RequestParam("file") MultipartFile file) {
        try {
            String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
            AnalysisResult result = analysisService.analyzeJavaFile(
                    sourceCode,
                    file.getOriginalFilename()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Анализировать zip фрхив",
            description = "Загрузите .zip файл для анализа"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Анализ успешно выполнен",
                    content = @Content(
                            schema = @Schema(implementation = AnalysisResult.class)
                    )
            )
    })
    @PostMapping(
            value = "/analyze-project",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AnalysisResult>>  analyzeProject(
            @Parameter(
                    description = "ZIP архив с Java проектом",
                    required = true
            )
            @RequestParam("file") MultipartFile zipFile) throws IOException {
        try{
            List<AnalysisResult> results = analysisService.analyzeZipArchive(
                    zipFile.getBytes(),
                    zipFile.getOriginalFilename());
            return ResponseEntity.ok(results);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}