package com.jarvis.Model.DTO;
import com.fasterxml.jackson.annotation.*;
import com.jarvis.Model.Entity.User;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResultDTO {
    private Long id;
    private String projectName;
    private String fileName;
    private String packageName;
    private List<String> classNames;
    private List<CodeProblemDTO> problems;
    private Boolean success;
    private String errorMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String analysisTime;
    private Integer problemCount;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    private List<String> aiExplanations;
}
