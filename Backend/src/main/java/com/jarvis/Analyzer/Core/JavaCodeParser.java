package com.jarvis.Analyzer.Core;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JavaCodeParser {
    private final JavaParser javaParser;

    public JavaCodeParser() {
        TypeSolver typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver()
        );

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration()
                .setSymbolResolver(symbolSolver)
                .setLanguageLevel(com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_17);
    }

    public Optional<CompilationUnit> parseFile(String sourceFile, String fileName) {
        try{
            log.info("Parsing file {}", fileName);
            ParseResult<CompilationUnit> result = this.javaParser.parse(sourceFile);

            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit compilationUnit = result.getResult().get();
                return Optional.of(compilationUnit);
            }
            else{
                return Optional.empty();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> extractPackageName(CompilationUnit cu) {
        return cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString);
    }

    public List<String> extractClassNames(CompilationUnit cu) {
        return cu.findAll(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class)
                .stream()
                .map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toList());
    }
}