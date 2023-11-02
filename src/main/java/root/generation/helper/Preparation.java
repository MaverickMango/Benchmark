package root.generation.helper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.bean.BugRepository;
import root.bean.benchmarks.Defects4JBug;
import root.generation.compiler.JavaJDKCompiler;
import root.generation.execution.ExternalTestExecutor;
import root.generation.execution.ITestExecutor;
import root.generation.execution.InternalTestExecutor;
import root.generation.parser.ASTJDTParser;
import root.generation.transformation.InputTransformer;
import root.generation.transformation.extractor.InputExtractor;
import root.generation.parser.ASTJavaParser;
import root.generation.parser.AbstractASTParser;
import root.util.ConfigurationProperties;

import javax.management.JMException;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.net.MalformedURLException;
import java.util.stream.Collectors;

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
    public String testExecutorName;
    public String binWorkingRoot;
    public String finalTestsInfoPath;
    public String externalProjRoot;
    public String jvmPath;
    public int waitTime;
    int globalID;
    public Set<String> binJavaClasses;
    public Set<String> binExecuteTestClasses;
    public AbstractASTParser parser;
    public Map<String, Object> ASTs;//CompilationUnit
    public List<String> compilerOptions;
    public InputExtractor inputExtractor;
    public InputTransformer inputTransformer;
    URL[] progURLs;

    public Preparation() throws IOException {
        String location = ConfigurationProperties.getProperty("location");
        complianceLevel = ConfigurationProperties.getProperty("complianceLevel");
        Path path = Paths.get(location).toAbsolutePath();
        binJavaDir = path.resolve(ConfigurationProperties.getProperty("binJavaDir")).normalize().toString();
        binTestDir = path.resolve(ConfigurationProperties.getProperty("binTestDir")).normalize().toString();
        srcJavaDir = path.resolve(ConfigurationProperties.getProperty("srcJavaDir")).normalize().toString();
        srcTestDir = path.resolve(ConfigurationProperties.getProperty("srcTestDir")).normalize().toString();
        String tmp = ConfigurationProperties.getProperty("dependencies");
        if (tmp != null) {
            dependencies = new HashSet<>();
            dependencies.addAll(Arrays.stream(tmp.split(File.pathSeparator)).map(
                    de -> path.resolve(de).normalize().toString()
            ).collect(Collectors.toList()));
        }

        tmp = ConfigurationProperties.getProperty("binExecuteTestClasses");
        if (tmp != null) {
            binExecuteTestClasses = new HashSet<>();
            binExecuteTestClasses.addAll(Arrays.asList(tmp.split(File.pathSeparator)));
        }
        javaClassesInfoPath = ConfigurationProperties.getProperty("javaClassesInfoPath");
        testClassesInfoPath = ConfigurationProperties.getProperty("testClassesInfoPath");

        testExecutorName = ConfigurationProperties.getProperty("testExecutorName");
        if (testExecutorName == null)
            testExecutorName = "ExternalTestExecutor";
        binWorkingRoot = ConfigurationProperties.getProperty("binWorkingRoot");

        String id = Helper.getRandomID();
        if (binWorkingRoot == null)
            binWorkingRoot = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "working_" + id;

        finalTestsInfoPath = ConfigurationProperties.getProperty("finalTestsInfoPath");
        if (finalTestsInfoPath == null)
            finalTestsInfoPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "finalTests_" + id + ".txt";

        externalProjRoot = ConfigurationProperties.getProperty("externalProjRoot");
        if (externalProjRoot == null)
            externalProjRoot = new File("external").getCanonicalPath();

        jvmPath = ConfigurationProperties.getProperty("jvmPath");
        if (jvmPath == null)
            jvmPath = System.getProperty("java.home") + "/bin/java";

        waitTime = ConfigurationProperties.getPropertyInt("waitTime");
        if (waitTime == 0)
            waitTime = 6000;

        globalID = 0;
    }

    public void initialize(boolean sourceOnly, boolean testOnly) throws IOException, ClassNotFoundException {
        if (!sourceOnly) {
            invokeClassFinder();//?
        }
        invokeCompilerOptionInitializer(complianceLevel);
        invokeSourceASTParser(testOnly);
        invokeTransformation();
        invokeProgURLsInitializer();
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

        parser = new ASTJavaParser(srcJavaDir, srcTestDir, dependencies, complianceLevel);//resolver是白给的么
//        parser = new ASTJDTParser(srcJavaDir, srcTestDir, dependencies, new HashMap<>());//很好这两个都没有类型解析啊啊啊
        if (!testOnly)
            parser.parseASTs(srcJavaDir);
        parser.parseASTs(srcTestDir);
        ASTs = parser.getASTs();

        logger.info("AST parsing is finished!");
    }

    void invokeProgURLsInitializer() throws MalformedURLException {
        List<String> tempList = new ArrayList<>();
        tempList.add(binJavaDir);
        tempList.add(binTestDir);
        if (dependencies != null)
            tempList.addAll(dependencies);
        progURLs = Helper.getURLs(tempList);
    }

    private void invokeCompilerOptionInitializer(String complianceLevel) {
        compilerOptions = new ArrayList<>();
        compilerOptions.add("-nowarn");
        compilerOptions.add("-source");
        compilerOptions.add(complianceLevel);
        compilerOptions.add("-cp");
        StringBuilder cpStr = new StringBuilder();
        if (dependencies != null) {
            for (String path: dependencies) {
                cpStr.append(File.pathSeparator).append(path);
            }
        } else {
            cpStr.append(File.pathSeparator).append(binJavaDir);
            cpStr.append(File.pathSeparator).append(binTestDir);
        }
        compilerOptions.add(cpStr.toString());
    }

    private void invokeTransformation() {
        Defects4JBug defects4JBug = new Defects4JBug(ConfigurationProperties.getProperty("proj"),
                ConfigurationProperties.getProperty("id"), ConfigurationProperties.getProperty("location"),
                ConfigurationProperties.getProperty("fixingCommit"), ConfigurationProperties.getProperty("buggyCommit"),
                ConfigurationProperties.getProperty("inducingCommit"), ConfigurationProperties.getProperty("originalCommit"));
        BugRepository bugRepository = new BugRepository(defects4JBug);
        inputTransformer = new InputTransformer(bugRepository);
        inputExtractor = new InputExtractor(parser);
        MutatorHelper.initialize();
    }

    public Map<String, JavaFileObject> getCompiledClassesForTestExecution(Map<String, String> javaSources) {
        JavaJDKCompiler compiler = new JavaJDKCompiler(ClassLoader.getSystemClassLoader(), compilerOptions);
        try {
            boolean isCompiled = compiler.compile(javaSources);
            if (isCompiled)
                return compiler.getClassLoader().getCompiledClasses();
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void compileClassesForTestExecution(Map<String, String> javaSources) {

    }
    protected ITestExecutor getTestExecutor(Map<String, JavaFileObject> compiledClasses, Set<String> executePosTests)
            throws JMException, IOException {
        if (testExecutorName.equalsIgnoreCase("ExternalTestExecutor")) {
            File binWorkingDirFile = new File(binWorkingRoot, "bin_" + (globalID++));
            IO.saveCompiledClasses(compiledClasses, binWorkingDirFile);
            String binWorkingDir = binWorkingDirFile.getCanonicalPath();
            //todo 这里需要区分原有的成功测试和失败测试么
            String tempPath = null;// (executePosTests == positiveTests) ? finalTestsInfoPath : null;
            return new ExternalTestExecutor(executePosTests, /*negativeTests*/null, tempPath, binJavaDir, binTestDir,
                    dependencies, binWorkingDir, externalProjRoot, jvmPath, waitTime);

        } else if (testExecutorName.equalsIgnoreCase("InternalTestExecutor")) {
            CustomURLClassLoader urlClassLoader = new CustomURLClassLoader(progURLs, compiledClasses);
            return new InternalTestExecutor(executePosTests, /*negativeTests*/null, urlClassLoader, waitTime);
        } else {
            logger.error("test executor name '" + testExecutorName + "' not found ");
            throw new JMException("Exception in getTestExecutor()");
        }
    }
}
