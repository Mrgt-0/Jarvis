package com.jarvis.Model.Mapper;
import com.jarvis.Model.DTO.AnalysisResultDTO;
import com.jarvis.Model.Entity.AnalysisResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalysisResultMapper {
    @Mapping(target = "id", source = "id")
    AnalysisResultDTO toDTO(AnalysisResult analysisResult);
    @Mapping(target = "id", source = "id")
    AnalysisResult toEntity(AnalysisResultDTO analysisResultDTO);
}