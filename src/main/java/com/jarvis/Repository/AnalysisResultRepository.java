package com.jarvis.Repository;
import com.jarvis.Model.Entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    //List<AnalysisResult> findByUserId(String userId);
    List<AnalysisResult> findByProjectName(String projectName);
    List<AnalysisResult> findByFileNameContainingIgnoreCase(String fileName);
    //long countByUserId(String userId);
}
