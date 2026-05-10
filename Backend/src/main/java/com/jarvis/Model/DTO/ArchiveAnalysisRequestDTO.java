package com.jarvis.Model.DTO;
import com.jarvis.Model.Entity.User;
import lombok.*;

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