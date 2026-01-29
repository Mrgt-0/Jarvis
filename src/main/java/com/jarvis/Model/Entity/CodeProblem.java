package com.jarvis.Model.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeProblem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id")
    private Long analysisId;
    private String fileName;
    @Column(name = "line_number")
    private Integer line;
    @Column(name = "column_number")
    private Integer column;
    private String message;
    private String severity;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private String ruleId;
}