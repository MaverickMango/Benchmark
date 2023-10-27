package root.generation.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ParserConfiguration;
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
                         Set<String> dependencies, String complianceLevel) throws IOException {
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
                .setLanguageLevel(getASTLevel(complianceLevel))//不设置level的话‘_’是保留字(reserved keyword)，设置后直接堆栈溢出
                .setSymbolResolver(javaSymbolSolver);
    }


    private ParserConfiguration.LanguageLevel getASTLevel(String complianceLevel) {
        ParserConfiguration.LanguageLevel level;
        switch (complianceLevel) {
            case "1.4":
            case "4":
                level = ParserConfiguration.LanguageLevel.JAVA_1_4;
                break;
            case "1.3":
            case "3":
                level = ParserConfiguration.LanguageLevel.JAVA_1_3;
                break;
            case "1.2":
            case "2":
                level = ParserConfiguration.LanguageLevel.JAVA_1_2;
                break;
            case "1.9":
            case "9":
                level = ParserConfiguration.LanguageLevel.JAVA_9;
                break;
            case "10":
                level = ParserConfiguration.LanguageLevel.JAVA_10;
                break;
            case "11":
                level = ParserConfiguration.LanguageLevel.JAVA_11;
                break;
            case "1.5":
            case "5":
            case "1.6":
            case "6":
            case "1.7":
            case "7":
            case "1.8":
            case "8":
            default:
                level = ParserConfiguration.LanguageLevel.JAVA_8;
        }
        return level;
    }

    /**
     * parse all ".java" file to CompilationUnit in the fileDir
     * @param fileDir java source code directory
     */
    @Override
    public void parseASTs(String fileDir) {
        String filePath = "";
        try {
            List<File> allFiles = FileUtils.findAllFiles(fileDir, ".java");
            for (File file : allFiles) {
                filePath = file.getAbsolutePath();
                CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                asts.put(file.getAbsolutePath(), compilationUnit);
            }
        } catch (Exception e) {
            logger.error("Error occurred when parsing AST: " + filePath + ": " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getASTs() {
        return asts;
    }
}
