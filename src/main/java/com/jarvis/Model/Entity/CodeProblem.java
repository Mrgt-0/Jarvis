package com.jarvis.Model.Entity;
import com.jarvis.Model.Enum.ProblemSeverity;
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
    private ProblemSeverity severity;

    public static CodeProblem of(String ruleId, String fileName, int line, int column, String snippet, String message) {
        return CodeProblem.builder()
                .ruleId(ruleId)
                .fileName(fileName)
                .line(line)
                .column(column)
                .snippet(snippet)
                .message(message)
                .severity(ProblemSeverity.MEDIUM)
                .build();
    }
}
