package com.jarvis.Service;
import com.jarvis.Analyzer.Core.JavaCodeParser;
import com.jarvis.Analyzer.Core.RuleEngine;
import com.jarvis.Model.Entity.AnalysisResult;
import com.jarvis.Model.Entity.CodeFile;
import com.jarvis.Model.Entity.CodeProblem;
import com.github.javaparser.ast.CompilationUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final JavaCodeParser javaCodeParser;
    private final RuleEngine ruleEngine;

    public AnalysisResult analyzeJavaFile(String sourceFile, String fileName) {
        log.info("Analyzing Java File {}", fileName);

        Optional< CompilationUnit> astOpt = javaCodeParser.parseFile(sourceFile, fileName);
        if(astOpt.isEmpty()){
            log.info("Error when parse file: {}", fileName);
            return AnalysisResult.error(fileName, "Error when parse file");
        }

        CompilationUnit ast = astOpt.get();
        List<CodeProblem> problems = ruleEngine.analyze(ast, sourceFile, fileName);
        String packageName = javaCodeParser.extractPackageName(ast)
                .orElse("default");
        List<String> classNames = javaCodeParser.extractClassNames(ast);

        log.info("Анализ файла {} завершен: {} проблем",
                fileName, problems.size());

        return AnalysisResult.success(fileName, packageName, classNames, problems);
    }

    public List<AnalysisResult> analyzeZipArchive(byte[] zipData, String archiveName) {
        log.info("Analyzing Zip Archive {}", archiveName);
        List<CodeFile> files = extractJavaFilesFromZip(zipData, archiveName);
        return files.stream()
                .map(file -> analyzeJavaFile(file.getSourceCode(), file.getFileName()))
                .collect(Collectors.toList());
    }

    private List<CodeFile> extractJavaFilesFromZip(byte[] zipData, String archiveName) {
        List<CodeFile> javaFiles = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                    String content = new String(zis.readAllBytes(), StandardCharsets.UTF_8);

                    CodeFile codeFile = new CodeFile();
                    codeFile.setFileName(entry.getName());
                    codeFile.setSourceCode(content);
                    javaFiles.add(codeFile);
                    log.debug("Извлечен Java файл из архива: {}", entry.getName());
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            log.error("Ошибка при чтении ZIP архива {}: {}", archiveName, e.getMessage());
        }

        log.info("Из архива {} извлечено {} Java файлов", archiveName, javaFiles.size());
        return javaFiles;
    }
}