package root;

import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.slicer.Slicer;
import root.analysis.soot.SootUpAnalyzer;
import root.entities.MultiFilesPatch;
import root.entities.SingleFilePatch;
import root.entities.benchmarks.Defects4JBug;
import root.entities.ci.BugWithHistory;
import root.util.PatchHelper;
import root.execution.compiler.JavaJDKCompiler;
import root.entities.Patch;
import root.execution.ExternalTestExecutor;
import root.execution.ITestExecutor;
import root.execution.InternalTestExecutor;
import root.generation.helper.*;
import root.generation.transformation.MutateHelper;
import root.analysis.parser.ASTJavaParser;
import root.analysis.parser.AbstractASTParser;
import root.generation.transformation.TransformHelper;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

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

public class ProjectPreparation {
    private static final Logger logger = LoggerFactory.getLogger(ProjectPreparation.class);
    public String complianceLevel;
    public String srcJavaDir;
    public String srcTestDir;
    public String binJavaDir;
    public String binTestDir;
    public Set<String> dependencies;
    public String patchesDir;
    public String sliceLog;
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
    public AbstractASTParser bugParser;
    public Slicer slicer;
    public Map<String, Object> ASTs;//CompilationUnit
    public List<String> compilerOptions;
    URL[] progURLs;
    public List<Patch> patches;//patch文件到bug文件
    public BugWithHistory bug;

    public ProjectPreparation() throws IOException {
        String proj = ConfigurationProperties.getProperty("proj");
        String bugId = ConfigurationProperties.getProperty("id");
        String location = ConfigurationProperties.getProperty("location");
        String originalCommit = ConfigurationProperties.getProperty("originalCommit");
        bug = new Defects4JBug(proj, bugId, location, originalCommit);

        complianceLevel = ConfigurationProperties.getProperty("complianceLevel");
        Path path = Paths.get(location).toAbsolutePath();
        binJavaDir = path.resolve(ConfigurationProperties.getProperty("binJavaDir")).normalize().toString();
        binTestDir = path.resolve(ConfigurationProperties.getProperty("binTestDir")).normalize().toString();
        srcJavaDir = path.resolve(ConfigurationProperties.getProperty("srcJavaDir")).normalize().toString();
        srcTestDir = path.resolve(ConfigurationProperties.getProperty("srcTestDir")).normalize().toString();

        String tmp = ConfigurationProperties.getProperty("dependencies");
        if (tmp != null) {
            dependencies = new HashSet<>();
            Path finalPath = path;
            dependencies.addAll(Arrays.stream(tmp.split(File.pathSeparator)).map(
                    de -> finalPath.resolve(de).normalize().toString()
            ).collect(Collectors.toList()));
        }
        tmp = ConfigurationProperties.getProperty("patchesDir");
        if (tmp != null) {
            patchesDir = Paths.get(tmp).toAbsolutePath().toString();
        }
        tmp = ConfigurationProperties.getProperty("sliceRoot");
        path = Paths.get(tmp).toAbsolutePath();
        sliceLog = path.resolve(proj.toLowerCase()).normalize().toString() + File.separator + bug.getBugName().toLowerCase() + File.separator + "trace.out.mapping";

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
//        if (!sourceOnly) {
//            invokeClassFinder();//?
//        }
//        invokeCompilerOptionInitializer(complianceLevel);
        invokeSourceASTParser(testOnly);
        invokePurify();
        invokeTransformer();
        invokeSlicer();
        if (patchesDir != null) {
            invokePatches();
        }
//        invokeProgURLsInitializer();
    }

    private void invokeClassFinder() throws ClassNotFoundException, IOException {
        ClassFinder finder = new ClassFinder(binJavaDir, binTestDir, dependencies);
        binJavaClasses = finder.findBinJavaClasses();

        if (binExecuteTestClasses == null)
            binExecuteTestClasses = finder.findBinExecuteTestClasses();

        if (javaClassesInfoPath != null)
            org.apache.commons.io.FileUtils.writeLines(new File(javaClassesInfoPath), binJavaClasses);
        if (testClassesInfoPath != null)
            org.apache.commons.io.FileUtils.writeLines(new File(testClassesInfoPath), binExecuteTestClasses);
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

    private void invokeSourceASTParser(boolean testOnly) throws IOException {
        logger.info("Invoking source code ast parser");
        bugParser = new ASTJavaParser(srcJavaDir, srcTestDir, dependencies, complianceLevel);//resolver是白给的么
//        parser = new ASTJDTParser(srcJavaDir, srcTestDir, dependencies, new HashMap<>());//很好这两个都没有类型解析啊啊啊
//        if (!testOnly)
//            parser.parseASTs(srcJavaDir);
//        parser.parseASTs(srcTestDir);
//        ASTs = parser.getASTs();
//        logger.info("AST parsing is finished!");
    }

    private void invokePurify() {
        logger.info("Invoking purify failing tests");
        //copy patch directory
        String purifiedTest = srcTestDir + "_purify";
        if (!FileUtils.notExists(purifiedTest)) {
            String[] cmd = new String[]{"/bin/bash", "-c", "cp -r " + purifiedTest + "/* " + srcTestDir};
            FileUtils.executeCommand(cmd);
        } else {
            //todo test purify

        }
    }

    private void invokeTransformer() {
        logger.info("Invoking transformerHelper and MutateHelper");
        SootUpAnalyzer analyzer = invokeSootAnalysis();
        Defects4JBug defects4JBug = (Defects4JBug) bug;
        String workingDir = defects4JBug.getWorkingDir();
        String orgDir = workingDir.replace("_buggy", "_org");
        if (FileUtils.notExists(orgDir)) {
            String[] cmd = new String[]{"/bin/bash", "-c", "cp -r " + workingDir + " " + orgDir};
            FileUtils.executeCommand(cmd);
        }
        Defects4JBug org = new Defects4JBug(defects4JBug.getProj(), defects4JBug.getId(), orgDir,
                defects4JBug.getFixingCommit(), defects4JBug.getBuggyCommit(), defects4JBug.getInducingCommit(), defects4JBug.getOriginalCommit());
        TransformHelper.initialize(defects4JBug, org, bugParser, analyzer);
        MutateHelper.initialize();
    }

    private void invokeSlicer() {
        logger.info("Invoking slicer for analysis");
        slicer = new Slicer(srcJavaDir, srcTestDir, sliceLog);
    }

    private void invokePatches() {
        logger.info("Invoking patches preprocessing");
        //copy patch directory
        String workingDir = ConfigurationProperties.getProperty("location");
        String patchDir = workingDir.replace("_buggy", "_pat");
        if (FileUtils.notExists(patchDir)) {
            String[] cmd = new String[]{"/bin/bash", "-c", "cp -r " + workingDir + " " + patchDir};
            FileUtils.executeCommand(cmd);
        }
        ConfigurationProperties.setProperty("patchDir", patchDir);
        SootUpAnalyzer analyzer = new SootUpAnalyzer(binJavaDir.replace(workingDir, patchDir),
                binTestDir.replace(workingDir, patchDir),
                dependencies, complianceLevel);
        Defects4JBug buggy = (Defects4JBug) bug;
        Defects4JBug pat = new Defects4JBug(buggy.getProj(), buggy.getId(), ConfigurationProperties.getProperty("patchDir"),
                buggy.getFixingCommit(), buggy.getBuggyCommit(), buggy.getInducingCommit(), buggy.getOriginalCommit());
        PatchHelper.initialization(pat, bugParser, analyzer);
        try {
            String[] split = patchesDir.split(File.pathSeparator);
            List<Patch> patches = new ArrayList<>();
            for (String dir :split) {
                logger.info("...Parsing patches in " + dir);
                List<String> allFiles = FileUtils.findAllFilePaths(dir, ".java");
                Patch patch;
                String filePath = allFiles.get(0);
                String patchHead = filePath.substring(0, filePath.indexOf(ConfigurationProperties.getProperty("srcJavaDir")) - 1);
                if (allFiles.size() == 1) {//single file patch
                    CompilationUnit unit = PatchHelper.ASTExtractor.getCompilationUnit(filePath);
                    patch = new SingleFilePatch(filePath, patchHead, unit);
                    patches.add(patch);
                    continue;
                }
                Map<String, List<String>> map = new HashMap<>();//<header, list<patchAbsPath>>
                map.put(patchHead, new ArrayList<>());
                map.get(patchHead).add(filePath);
                for (int i = 1; i < allFiles.size(); i ++) {
                    String path = allFiles.get(i);
                    patchHead = path.substring(0, path.indexOf(ConfigurationProperties.getProperty("srcJavaDir")) - 1);
                    if (!map.containsKey(patchHead)) {
                        map.put(patchHead, new ArrayList<>());
                    }
                    map.get(patchHead).add(path);
                }
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    List<String> paths = entry.getValue();
                    String patchAbsPath = paths.get(0);
                    String fileHeader = entry.getKey();
                    if (paths.size() == 1) {
                        CompilationUnit unit = PatchHelper.ASTExtractor.getCompilationUnit(patchAbsPath);
                        patch = new SingleFilePatch(patchAbsPath, fileHeader, unit);
                    } else {
                        CompilationUnit unit = PatchHelper.ASTExtractor.getCompilationUnit(patchAbsPath);
                        patch = new MultiFilesPatch(patchAbsPath, fileHeader, unit);
                        for (int i = 1; i < paths.size(); i++) {
                            patchAbsPath = paths.get(i);
                            unit = PatchHelper.ASTExtractor.getCompilationUnit(patchAbsPath);
                            ((MultiFilesPatch) patch).addSingleFile(patchAbsPath, fileHeader, unit);
                        }
                    }
                    patches.add(patch);
                }
            }
            if (patches.isEmpty()) {
                throw new IllegalArgumentException("patch file should be '.java' file");
            }
            this.patches = patches;
        } catch (Exception e) {
            //TODO: parse the diff file and get the patch file
            logger.error("Invalid patch file path: " + e.getMessage());
        }
    }

    private SootUpAnalyzer invokeSootAnalysis() {
        SootUpAnalyzer analyzer = new SootUpAnalyzer(binJavaDir, binTestDir, dependencies, complianceLevel);
        return analyzer;
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
