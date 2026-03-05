package com.jarvis.Service;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.Entity.AnalysisResult;
import com.jarvis.Model.Entity.CodeProblem;
import com.jarvis.Model.Mapper.CodeProblemMapper;
import com.jarvis.Repository.AnalysisResultRepository;
import com.jarvis.Repository.CodeProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AnalysisStorageService {
    private final AnalysisResultRepository resultRepository;
    private final CodeProblemRepository codeProblemRepository;
    private final CodeProblemMapper codeProblemMapper;

    @Transactional
    public void saveAnalysisResult(AnalysisResultDTO dto) {
        try {
            log.info("Сохранение анализа файла {} в БД...", dto.getFileName());
            if (dto.getUser() == null)
                throw new IllegalArgumentException("Нельзя сохранять анализ без пользователя. User не может быть null.");

            AnalysisResult entity = new AnalysisResult();
            entity.setProjectName(dto.getProjectName() != null ? dto.getProjectName() : "anonymous");
            entity.setFileName(dto.getFileName());
            entity.setPackageName(dto.getPackageName() != null ? dto.getPackageName() : "");
            entity.setUser(dto.getUser());
            if (dto.getClassNames() != null)
                entity.setClassNames(dto.getClassNames());
            else
                entity.setClassNames(new ArrayList<>());

            entity.setSuccess(dto.getSuccess() != null ? dto.getSuccess() : false);
            entity.setErrorMessage(dto.getErrorMessage() != null ? dto.getErrorMessage() : "");
            entity.setAnalysisTime(LocalDateTime.now());

            int problemCount = (dto.getProblems() != null) ? dto.getProblems().size() : 0;
            entity.setProblemCount(problemCount);

            AnalysisResult savedAnalysis = resultRepository.saveAndFlush(entity);
            log.info("Анализ сохранен с ID: {} для файла: {}", savedAnalysis.getId(), savedAnalysis.getFileName());

            if (dto.getProblems() != null && !dto.getProblems().isEmpty()) {
                log.info("Сохранение {} проблем для анализа ID: {}", dto.getProblems().size(), savedAnalysis.getId());

                List<CodeProblem> codeProblems = dto.getProblems().stream()
                        .map(problemDto -> {
                            CodeProblem problem = codeProblemMapper.toEntity(problemDto);
                            problem.setAnalysisId(savedAnalysis.getId());
                            problem.setCreatedAt(LocalDateTime.now());

                            if (problem.getMessage() == null)
                                problem.setMessage("No description");
                            if (problem.getSeverity() == null)
                                problem.setSeverity("MEDIUM");
                            if (problem.getFileName() == null)
                                problem.setFileName(dto.getFileName());

                            return problem;
                        })
                        .collect(Collectors.toList());

                List<CodeProblem> savedProblems = codeProblemRepository.saveAll(codeProblems);
                log.info("Сохранено {} проблем для анализа ID: {}", savedProblems.size(), savedAnalysis.getId());
            }

            log.info("Успешно сохранен анализ файла {} в БД. ID: {}, проблем: {}",
                    dto.getFileName(), savedAnalysis.getId(), problemCount);

        } catch (Exception e) {
            log.error("Ошибка сохранения в БД для файла {}: {}", dto.getFileName(), e.getMessage(), e);
        }
    }
}