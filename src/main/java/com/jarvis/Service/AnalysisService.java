package com.jarvis.Service;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.DTO.ArchiveAnalysisRequestDTO;
import java.util.List;

public interface AnalysisService {
    AnalysisResultDTO analyzeJavaFile(String sourceCode, String fileName);
    List<AnalysisResultDTO> analyzeZipArchive(ArchiveAnalysisRequestDTO request);
}