package com.jarvis.Model.DTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeProblemDTO {
    private String fileName;
    private Integer line;
    private Integer column;
    private String message;
    private String severity;
    private LocalDateTime createdAt;
    private String ruleId;
    @Builder.Default
    private String snippet = "";
}