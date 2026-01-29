package com.jarvis.Service;
import com.jarvis.Model.Entity.CodeFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArchiveProcessingService {

    public List<CodeFile> extractJavaFilesFromZip(byte[] zipData, String archiveName) {
        List<CodeFile> javaFiles = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();

                if (!entry.isDirectory() && fileName.toLowerCase().endsWith(".java")) {
                    byte[] fileBytes = zis.readAllBytes();

                    if (fileBytes.length == 0) {
                        log.warn("Файл {} пустой, пропускаем", fileName);
                        continue;
                    }
                    String cleanedContent = cleanJavaFileContent(fileBytes, fileName);

                    CodeFile codeFile = new CodeFile();
                    codeFile.setFileName(fileName);
                    codeFile.setSourceCode(cleanedContent);
                    javaFiles.add(codeFile);
                    log.debug("Извлечен Java файл из архива: {} ({} байт)", fileName, fileBytes.length);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            log.error("Ошибка при чтении ZIP архива {}: {}", archiveName, e.getMessage());
        }
        log.info("Из архива {} извлечено {} Java файлов", archiveName, javaFiles.size());
        return javaFiles;
    }

    private String cleanJavaFileContent(byte[] fileBytes, String fileName) {
        try {
            String content = detectEncodingAndConvert(fileBytes);
            content = removeProblematicCharacters(content);
            if (!isLikelyValidJava(content)) {
                log.warn("Файл {} не похож на валидный Java код", fileName);
            }
            return content;

        } catch (Exception e) {
            log.error("Ошибка очистки файла {}: {}", fileName, e.getMessage());
            return new String(fileBytes, StandardCharsets.UTF_8)
                    .replace("\u0000", "")
                    .replace("\uFEFF", "");
        }
    }

    private String detectEncodingAndConvert(byte[] bytes) {
        try {
            String utf8 = new String(bytes, StandardCharsets.UTF_8);
            if (!utf8.contains("\uFFFD")) {
                return utf8;
            }
        } catch (Exception ignored) {}
        try {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        } catch (Exception ignored) {}
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String removeProblematicCharacters(String content) {
        if (content == null) return "";
        content = content.replace("\uFEFF", "");
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') {
                cleaned.append(c);
            } else if (c >= 32 && c <= 126) {
                cleaned.append(c);
            } else if (c >= 1040 && c <= 1103) {
                cleaned.append(c);
            } else if (c == 1025 || c == 1105) {
                cleaned.append(c);
            } else if (c == '\uFFFD') {
                cleaned.append(' ');
            } else {
                if (Character.isLetterOrDigit(c) || Character.isWhitespace(c))
                    cleaned.append(c);
                else
                    cleaned.append(' ');
            }
        }
        return cleaned.toString();
    }

    private boolean isLikelyValidJava(String content) {
        if (content == null || content.trim().isEmpty())
            return false;

        String trimmed = content.trim();
        return trimmed.contains("class ") ||
                trimmed.contains("public ") ||
                trimmed.contains("private ") ||
                trimmed.contains("protected ") ||
                trimmed.contains("import ") ||
                trimmed.contains("package ");
    }
}