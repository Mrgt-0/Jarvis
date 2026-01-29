package com.jarvis.Service;
import com.jarvis.Model.DTO.*;
import com.jarvis.Model.Entity.AnalysisResult;
import com.jarvis.Model.Entity.CodeFile;
import com.jarvis.Model.Entity.CodeProblem;
import com.jarvis.Model.Mapper.AnalysisResultMapper;
import com.jarvis.Model.Mapper.CodeProblemMapper;
import com.jarvis.Repository.AnalysisResultRepository;
import com.jarvis.Repository.CodeProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
public class AnalysisServiceImpl implements AnalysisService {
    private final FileAnalyzer fileAnalyzer;
    private final AnalysisStorageService storageService;
    private final ArchiveAnalysisOrchestrator archiveOrchestrator;
    private final AnalysisResultMapper analysisResultMapper;

    @Override
    public AnalysisResultDTO analyzeJavaFile(String sourceCode, String fileName) {
        log.info("Анализ файла: {}", fileName);

        try {
            AnalysisRequestDTO analysisRequestDTO = new AnalysisRequestDTO(sourceCode, fileName);
            AnalysisResultDTO result = fileAnalyzer.analyze(analysisRequestDTO);
            storageService.saveAnalysisResult(result);
            return result;
        } catch (Exception e) {
            log.error("Ошибка анализа файла {}: {}", fileName, e.getMessage());
            return AnalysisResultDTO.error(fileName, e.getMessage());
        }
    }

    @Override
    public List<AnalysisResultDTO> analyzeZipArchive(ArchiveAnalysisRequestDTO request) {
        return archiveOrchestrator.analyzeArchive(request);
    }
}