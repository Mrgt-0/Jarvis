package com.jarvis.Service;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.DTO.ArchiveAnalysisRequestDTO;
import com.jarvis.Model.Entity.User;
import java.util.List;

public interface AnalysisService {
    AnalysisResultDTO analyzeJavaFile(String sourceCode, String fileName, User user);
    List<AnalysisResultDTO> analyzeZipArchive(ArchiveAnalysisRequestDTO request);
}