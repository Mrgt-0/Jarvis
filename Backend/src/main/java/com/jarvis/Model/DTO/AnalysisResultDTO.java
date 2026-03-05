package com.jarvis.Model.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jarvis.Model.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResultDTO {
    private String projectName;
    private String fileName;
    private String packageName;
    private List<String> classNames;
    private List<CodeProblemDTO> problems;
    private Boolean success;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisTime;
    private Integer problemCount;
    private User user;

    public static AnalysisResultDTO success(String fileName, String packageName,
                                            List<String> classNames, List<CodeProblemDTO> problems) {
        return AnalysisResultDTO.builder()
                .fileName(fileName)
                .packageName(packageName)
                .classNames(classNames)
                .problems(problems)
                .success(true)
                .analysisTime(LocalDateTime.now())
                .build();
    }

    public static AnalysisResultDTO error(String fileName, String errorMessage) {
        return AnalysisResultDTO.builder()
                .fileName(fileName)
                .success(false)
                .errorMessage(errorMessage)
                .analysisTime(LocalDateTime.now())
                .problems(List.of())
                .classNames(List.of())
                .build();
    }

    public boolean isSuccess() {
        return success;
    }
}
