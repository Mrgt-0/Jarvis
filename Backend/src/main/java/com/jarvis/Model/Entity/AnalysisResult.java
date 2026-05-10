package com.jarvis.Model.Entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

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

    @ElementCollection
    @CollectionTable(
            name = "analysis_explanations",
            joinColumns = @JoinColumn(name = "analysis_result_id")
    )
    @Column(name = "explanation_text")
    private List<String> aiExplanations = new ArrayList<>();

    private Boolean success;
    private String errorMessage;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime analysisTime;
    private Integer problemCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}