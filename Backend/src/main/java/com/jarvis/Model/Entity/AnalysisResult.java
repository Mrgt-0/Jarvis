package com.jarvis.Model.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<String> classNames = new ArrayList<>();

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeProblem> problems = new ArrayList<>();

    private Boolean success;
    private String errorMessage;
    private LocalDateTime analysisTime;
    private Integer problemCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}