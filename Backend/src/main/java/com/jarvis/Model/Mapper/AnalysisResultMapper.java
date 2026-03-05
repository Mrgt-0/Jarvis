package com.jarvis.Model.Mapper;

import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.Entity.AnalysisResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnalysisResultMapper {
    AnalysisResultDTO toDTO(AnalysisResult analysisResult);
    AnalysisResult toEntity(AnalysisResultDTO analysisResultDTO);
}
