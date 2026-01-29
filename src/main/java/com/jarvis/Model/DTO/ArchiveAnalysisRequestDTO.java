package com.jarvis.Model.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveAnalysisRequestDTO {
    private String projectName;
    private byte[] zipData;
    private String archiveName;

    public static ArchiveAnalysisRequestDTO fromMultipart(MultipartFile file, String projectName) {
        try {
            return ArchiveAnalysisRequestDTO.builder()
                    .projectName(projectName)
                    .archiveName(file.getOriginalFilename())
                    .zipData(file.getBytes())
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось создать запрос из файла", e);
        }
    }

    // Проверка валидности
    public boolean isValid() {
        return zipData != null && zipData.length > 0
                && archiveName != null && !archiveName.trim().isEmpty();
    }
}