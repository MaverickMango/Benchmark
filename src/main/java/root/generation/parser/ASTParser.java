package root.generation.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import root.util.FileUtils;


public class ASTParser {
    Map<String, CompilationUnit> asts;

    public ASTParser(String srcJavaDir, String srcTestDir,
                     Set<String> dependencies) throws IOException {
        this.asts = new HashMap<>();

        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaTypeSolver = new JavaParserTypeSolver(srcJavaDir);
        TypeSolver testTypeSolver = new JavaParserTypeSolver(srcTestDir);
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(reflectionTypeSolver);
        combinedTypeSolver.add(javaTypeSolver);
        combinedTypeSolver.add(testTypeSolver);
        if (dependencies != null) {
            for (String path :dependencies) {
                //todo: check whether path is a jar file or a directory.
                JarTypeSolver jarTypeSolver = new JarTypeSolver(path);
                combinedTypeSolver.add(jarTypeSolver);
            }
        }

        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration()
                .setAttributeComments(true)
                .setSymbolResolver(javaSymbolSolver);
    }

    public void parseASTs(String fileDir) throws IOException {
        List<File> allFiles = FileUtils.findAllFiles(fileDir, ".java");
        for (File file :allFiles) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(file);
            asts.put(file.getAbsolutePath(), compilationUnit);
        }
    }

    public Map<String, CompilationUnit> getASTs() {
        return asts;
    }
}
