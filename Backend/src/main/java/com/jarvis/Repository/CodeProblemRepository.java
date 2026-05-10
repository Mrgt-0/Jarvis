package com.jarvis.Repository;
import com.jarvis.Model.Entity.AnalysisResult;
import com.jarvis.Model.Entity.CodeProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeProblemRepository extends JpaRepository<CodeProblem, Long> {
    List<CodeProblem> findByAnalysisResult(AnalysisResult analysisResult);
}