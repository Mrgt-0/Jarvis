package com.jarvis.Model.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRequestDTO {
    //private String projectName;
    private String sourceCode;
    private String fileName;
}