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


public class ASTJavaParser extends AbstractASTParser {

    public ASTJavaParser(String srcJavaDir, String srcTestDir,
                         Set<String> dependencies) throws IOException {
        super(srcJavaDir, srcTestDir, dependencies);
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
                File de = new File(path);
                TypeSolver typeSolver;
                if (de.isDirectory())
                    typeSolver = new JavaParserTypeSolver(de);
                else
                    typeSolver = new JarTypeSolver(de);
                combinedTypeSolver.add(typeSolver);
            }
        }

        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration()
                .setAttributeComments(true)
                .setSymbolResolver(javaSymbolSolver);
    }

    /**
     * parse all ".java" file to CompilationUnit in the fileDir
     * @param fileDir java source code directory
     */
    @Override
    public void parseASTs(String fileDir) {
        try {
            List<File> allFiles = FileUtils.findAllFiles(fileDir, ".java");
            for (File file : allFiles) {
                CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                asts.put(file.getAbsolutePath(), compilationUnit);
            }
        } catch (IOException e) {
            logger.error("Error occurred when parsing AST: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getASTs() {
        return asts;
    }
}
