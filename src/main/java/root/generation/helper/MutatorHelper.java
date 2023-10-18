package root.generation.helper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.parser.ASTJDTParser;
import root.generation.parser.ASTJavaParser;
import root.generation.parser.AbstractASTParser;
import root.util.ConfigurationProperties;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MutatorHelper {
    private static final Logger logger = LoggerFactory.getLogger(MutatorHelper.class);
    protected String complianceLevel;
    protected String srcJavaDir;
    protected String srcTestDir;
    protected String binJavaDir;
    protected String binTestDir;
    protected Set<String> dependencies;
    protected String javaClassesInfoPath;
    protected String testClassesInfoPath;
    protected Set<String> binJavaClasses;
    protected Set<String> binExecuteTestClasses;
    AbstractASTParser parser;
    protected Map<String, Object> ASTs;//CompilationUnit
    protected List<String> compilerOptions;

    public MutatorHelper() {
        complianceLevel = ConfigurationProperties.getProperty("complianceLevel");
        String location = ConfigurationProperties.getProperty("location");
        binJavaDir = location + File.separator + ConfigurationProperties.getProperty("binJavaDir");
        binTestDir = location + File.separator + ConfigurationProperties.getProperty("binTestDir");
        srcJavaDir = location + File.separator + ConfigurationProperties.getProperty("srcJavaDir");
        srcTestDir = location + File.separator + ConfigurationProperties.getProperty("srcTestDir");
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
        invokeExtractors();
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

    private void invokeExtractors() {

    }
}
