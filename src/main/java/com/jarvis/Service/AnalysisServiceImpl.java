package com.jarvis.Service;
import com.jarvis.Model.DTO.*;
import com.jarvis.Model.Entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalysisServiceImpl implements AnalysisService {
    private final FileAnalyzer fileAnalyzer;
    private final AnalysisStorageService storageService;
    private final ArchiveAnalysisOrchestrator archiveOrchestrator;

    @Override
    public AnalysisResultDTO analyzeJavaFile(String sourceCode, String fileName, User user) {
        log.info("Анализ файла: {}", fileName);

        try {
            AnalysisRequestDTO analysisRequestDTO = new AnalysisRequestDTO(sourceCode, fileName);
            AnalysisResultDTO result = fileAnalyzer.analyze(analysisRequestDTO);
            result.setUser(user);
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