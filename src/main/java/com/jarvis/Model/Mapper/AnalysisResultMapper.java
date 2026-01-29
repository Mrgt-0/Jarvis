package com.jarvis.Model.Mapper;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.Entity.AnalysisResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnalysisResultMapper {
    AnalysisResult toEntity(AnalysisResultDTO dto);
    AnalysisResultDTO toDTO(AnalysisResult entity);
}
