package com.jarvis.Model.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "analysis_results")
@Data
public class AnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String projectName;
    private String fileName;
    private String packageName;

    @ElementCollection
    @CollectionTable(
            name = "analysis_classes",
            joinColumns = @JoinColumn(name = "analysis_result_id")
    )
    @Column(name = "class_name")
    private List<String> classNames;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "analysis_id")
    private List<CodeProblem> problems;

    private Boolean success;
    private String errorMessage;
    private LocalDateTime analysisTime;
    private Integer problemCount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}