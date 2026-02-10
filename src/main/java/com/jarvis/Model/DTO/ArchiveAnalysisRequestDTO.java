package com.jarvis.Model.DTO;
import com.jarvis.Model.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveAnalysisRequestDTO {
    private String projectName;
    private byte[] zipData;
    private String archiveName;
    private User user;
}