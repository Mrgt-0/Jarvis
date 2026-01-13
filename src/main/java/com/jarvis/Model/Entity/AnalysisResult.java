package com.jarvis.Model.Entity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResult {
    @JsonProperty("filename")
    private String fileName;
    @JsonProperty("packageName")
    private String packageName;
    @JsonProperty("classNames")
    private List<String> classNames;
    @JsonProperty("problems")
    private List<CodeProblem>  problems;
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("errorMessage")
    private String errorMessage;
    @JsonProperty("analysisTime")
    private LocalDateTime analysisTime;

    public static AnalysisResult success(String fileName, String packageName, List<String> classNames, List<CodeProblem> problems) {
        return AnalysisResult.builder()
                .fileName(fileName)
                .packageName(packageName)
                .classNames(classNames)
                .problems(problems)
                .success(true)
                .analysisTime(LocalDateTime.now())
                .build();
    }

    public static AnalysisResult error(String fileName, String errorMessage) {
        return AnalysisResult.builder()
                .fileName(fileName)
                .success(false)
                .errorMessage(errorMessage)
                .analysisTime(LocalDateTime.now())
                .build();
    }
}