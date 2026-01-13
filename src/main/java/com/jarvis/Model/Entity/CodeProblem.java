package com.jarvis.Model.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CodeProblem {
    private String ruleId;
    private String fileName;
    private int line;
    private int column;
    private String snippet;
    private String message;

    public static CodeProblem of(String ruleId, String fileName, int line, int column, String snippet, String message) {
        return CodeProblem.builder()
                .ruleId(ruleId)
                .fileName(fileName)
                .line(line)
                .column(column)
                .snippet(snippet)
                .message(message)
                .build();
    }
}
