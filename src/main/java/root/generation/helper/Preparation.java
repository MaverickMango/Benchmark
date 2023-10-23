package root.generation.helper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.transformation.InputTransformer;
import root.generation.transformation.extractor.InputExtractor;
import root.generation.parser.ASTJavaParser;
import root.generation.parser.AbstractASTParser;
import root.util.ConfigurationProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Preparation {
    private static final Logger logger = LoggerFactory.getLogger(Preparation.class);
    public String complianceLevel;
    public String srcJavaDir;
    public String srcTestDir;
    public String binJavaDir;
    public String binTestDir;
    public Set<String> dependencies;
    public String javaClassesInfoPath;
    public String testClassesInfoPath;
    public Set<String> binJavaClasses;
    public Set<String> binExecuteTestClasses;
    public AbstractASTParser parser;
    public Map<String, Object> ASTs;//CompilationUnit
    public List<String> compilerOptions;
    public InputExtractor inputExtractor;
    public InputTransformer inputTransformer;

    public Preparation() {
        complianceLevel = ConfigurationProperties.getProperty("complianceLevel");
        String location = ConfigurationProperties.getProperty("location");
        Path path = Paths.get(location).toAbsolutePath();
        binJavaDir = path.resolve(ConfigurationProperties.getProperty("binJavaDir")).normalize().toString();
        binTestDir = path.resolve(ConfigurationProperties.getProperty("binTestDir")).normalize().toString();
        srcJavaDir = path.resolve(ConfigurationProperties.getProperty("srcJavaDir")).normalize().toString();
        srcTestDir = path.resolve(ConfigurationProperties.getProperty("srcTestDir")).normalize().toString();
        String tmp = ConfigurationProperties.getProperty("dependencies");
        if (tmp != null) {
            dependencies = new HashSet<>();
            dependencies.addAll(Arrays.asList(tmp.split(File.pathSeparator)));
        }

        tmp = ConfigurationProperties.getProperty("binExecuteTestClasses");
        if (tmp != null) {
            binExecuteTestClasses = new HashSet<>();
            binExecuteTestClasses.addAll(Arrays.asList(tmp.split(File.pathSeparator)));
        }
        javaClassesInfoPath = ConfigurationProperties.getProperty("javaClassesInfoPath");
        testClassesInfoPath = ConfigurationProperties.getProperty("testClassesInfoPath");
    }

    public void initialize(boolean sourceOnly, boolean testOnly) throws IOException, ClassNotFoundException {
        if (!sourceOnly) {
            invokeClassFinder();//?
            invokeCompilerOptionInitializer(complianceLevel);
        }
        invokeSourceASTParser(testOnly);
        invokeTransformation();
    }

    private void invokeClassFinder() throws ClassNotFoundException, IOException {
        ClassFinder finder = new ClassFinder(binJavaDir, binTestDir, dependencies);
        binJavaClasses = finder.findBinJavaClasses();

        if (binExecuteTestClasses == null)
            binExecuteTestClasses = finder.findBinExecuteTestClasses();

        if (javaClassesInfoPath != null)
            FileUtils.writeLines(new File(javaClassesInfoPath), binJavaClasses);
        if (testClassesInfoPath != null)
            FileUtils.writeLines(new File(testClassesInfoPath), binExecuteTestClasses);
    }

    private void invokeSourceASTParser(boolean testOnly) throws IOException {
        logger.info("Invoking source code ast parse...");

        parser = new ASTJavaParser(srcJavaDir, srcTestDir, dependencies);//resolver是白给的么
//        parser = new ASTJDTParser(srcJavaDir, srcTestDir, dependencies, new HashMap<>());//很好这两个都没有类型解析啊啊啊
        if (!testOnly)
            parser.parseASTs(srcJavaDir);
        parser.parseASTs(srcTestDir);
        ASTs = parser.getASTs();

        logger.info("AST parsing is finished!");
    }

    private void invokeCompilerOptionInitializer(String complianceLevel) {
        compilerOptions = new ArrayList<>();
        compilerOptions.add("-nowarn");
        compilerOptions.add("-source");
        compilerOptions.add(complianceLevel);
        compilerOptions.add("-cp");
        StringBuilder cpStr = new StringBuilder(binJavaDir);
        if (dependencies != null) {
            for (String path: dependencies) {
                cpStr.append(File.pathSeparator).append(path);
            }
        }
        compilerOptions.add(cpStr.toString());
    }

    private void invokeTransformation() {
        //todo: test extractor/ assert / arguments
        inputExtractor = new InputExtractor(parser);
        MutatorHelper.initialize();
        inputTransformer = new InputTransformer();
    }
}
