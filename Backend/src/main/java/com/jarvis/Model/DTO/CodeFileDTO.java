package com.jarvis.Model.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeFileDTO {
    private String fileName;
    private String content;
    private long size;
    private String encoding;
}