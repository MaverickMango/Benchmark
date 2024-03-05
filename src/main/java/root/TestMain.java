package root;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.diff.DiffExtractor;
import root.entities.*;
import root.generation.entities.Input;
import root.generation.entities.Skeleton;
import root.generation.transformation.TransformHelper;
import root.generation.transformation.visitor.PathVisitor;
import root.generation.transformation.visitor.VariableVisitor;
import root.util.*;

import java.io.File;
import java.util.*;

public class TestMain extends AbstractMain {

    private static final Logger logger = LoggerFactory.getLogger(TestMain.class);

    public static CommandSummary setInputs(String location, List<String> strings) {
        CommandSummary cs = new CommandSummary();
        String bugName, srcJavaDir, srcTestDir, binJavaDir, binTestDir, testInfos, projectCP, originalCommit, cleaned, complianceLevel;
        bugName = strings.get(0);
        srcJavaDir = strings.get(1);
        srcTestDir = strings.get(2);
        binJavaDir = strings.get(3);
        binTestDir = strings.get(4);
        testInfos = strings.get(5);
        projectCP = strings.get(6);
        originalCommit = strings.get(7);
//        complianceLevel = strings.get(8);
        cleaned = strings.get(8);
        cs.append("-proj", bugName.split("_")[0]);
        cs.append("-id", bugName.split("_")[1]);
        cs.append("-location", location + bugName + "_buggy");
        cs.append("-srcJavaDir", srcJavaDir);
        cs.append("-srcTestDir", srcTestDir);
        cs.append("-binJavaDir", binJavaDir);
        cs.append("-binTestDir", binTestDir);
        cs.append("-testInfos", testInfos);
        cs.append("-dependencies", projectCP);
        cs.append("-originalCommit", originalCommit);
//        cs.append("-complianceLevel", "1.6");
        return cs;
    }

    public static void main(String[] args) {
        String location = args[0]; // "/home/liumengjiao/Desktop/CI/bugs/";
        String info = args[1]; // "/home/liumengjiao/Desktop/CI/Benchmark_py/generation/info/patches_inputs.csv";
        String patchesRootDir = args[2];
        String sliceRoot = args[3];//"/home/liumengjiao/Desktop/CI/Benchmark_py/slice/results/";
        List<List<String>> lists = FileUtils.readCsv(info, true);
        CommandSummary cs;
        for (int i = 35; i < lists.size(); i ++) {
            List<String> strings  = lists.get(i);
            cs = setInputs(location, strings);
            String patchesDir = getPatchDirByBug(strings.get(0), patchesRootDir);
            cs.append("-patchesDir", patchesDir);
            cs.append("-sliceRoot", sliceRoot);
            boolean res = executeMainProcess(cs);
            break;
        }
    }

    private static boolean executeMainProcess(CommandSummary cs) {
        TestMain main = new TestMain();
        logger.info("Start Initialization.");
        ProjectPreparation projectPreparation = main.initialize(cs.flat());
        if (projectPreparation == null) {
            return false;
        }
        String[] tests = ConfigurationProperties.getProperty("testInfos").split("#");
        Map<String, List<String>> testsByClazz = getTestInfos(tests);
        long seconds = TimeUtil.deltaInSeconds(main.bornTime);
        main.currentStat.addGeneralStat(Stats.General.INITIALIZATION_TIME, seconds);
//        logger.info("Initialization end with time cost " + seconds + "s");

        logger.info("Start diff extraction.");
        Date startDate = new Date();
        List<Difference> differences = diffExtraction(projectPreparation);
        seconds = TimeUtil.deltaInSeconds(startDate);
        main.currentStat.addGeneralStat(Stats.General.DIFF_TIME, seconds);
//        logger.info("Diff extraction end with time cost " + seconds + "s");

        pathSolver(projectPreparation, testsByClazz, differences);

        logger.info("Start test generation.");
        startDate = new Date();
        List<Skeleton> mutateRes = testGeneration(projectPreparation, testsByClazz, differences);
        seconds = TimeUtil.deltaInSeconds(startDate);
        main.currentStat.addGeneralStat(Stats.General.GENERATION_TIME, seconds);
//        logger.info("Test generation end with time cost " + seconds + "s");

        logger.info("Start patch validation.");
        startDate = new Date();
        boolean allCorrect = patchValidation(projectPreparation, mutateRes);
        seconds = TimeUtil.deltaInSeconds(startDate);
        main.currentStat.addGeneralStat(Stats.General.VALIDATION_TIME, seconds);
//        logger.info("Patch validation end with time cost " + seconds + "s");

        long totalSeconds = TimeUtil.deltaInSeconds(main.bornTime);
        main.currentStat.addGeneralStat(Stats.General.TOTAL_TIME, seconds);
        logger.info("Finish with total running time: " + totalSeconds + "s");

        String resultOutput = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + projectPreparation.bug.getBugName() + File.separator + "stats";
        FileUtils.writeToFile(main.currentStat.toString(), resultOutput, false);
        return allCorrect;
    }

    private static Map<String, List<String>> getTestInfos(String[] tests) {
        Map<String, List<String>> testsByClazz = new HashMap<>();
        logger.info("Preprocessing --------------------");
        logger.info("Split test one by one...");
        for (String triggerTest : tests) {
            String[] split1 = triggerTest.split(":");
            String clazzName = split1[0];//.replaceAll("\\.", File.separator);
            String methodName = split1[1];
            int lineNumber = split1.length == 3 ? Integer.parseInt(split1[2]) : 0;
            if (!testsByClazz.containsKey(clazzName)) {
                testsByClazz.put(clazzName, new ArrayList<>());
            }
            testsByClazz.get(clazzName).add(methodName + ":" + lineNumber);
        }
        logger.info("End preprocessing --------------------");
        return testsByClazz;
    }

    private static List<Difference> diffExtraction(ProjectPreparation projectPreparation) {
        DiffExtractor diffExtractor = new DiffExtractor();
        List<Difference> differences = new ArrayList<>();
        List<Patch> patches = projectPreparation.patches;
        logger.info("Processing differences extraction --------------------");
        for (Patch patch :patches) {
            Difference difference = diffExtractor.getDifferenceForPatch(patch);
            differences.add(difference);
        }
        logger.info("End differences extraction --------------------");
        return differences;
    }


    private static List<List<ExecutionPathInMth>> pathSolver(ProjectPreparation projectPreparation, Map<String, List<String>> testsByClazz, List<Difference> differences) {
        List<List<ExecutionPathInMth>> map = new ArrayList<>();//每个测试对应的执行路径
        for (Map.Entry<String, List<String>> entry :testsByClazz.entrySet()) {
            String clazzName = entry.getKey();//qualified class name
            for (String testName: entry.getValue()) {
                List<ExecutionPathInMth> paths = pathSolver(projectPreparation, projectPreparation.sliceLog);
//                List<ExecutionPathInMth> paths = pathSolverForSlicer4J(projectPreparation, projectPreparation.sliceLog);//todo 切片存放的路径不对
                /*
                 * 1. 提取differences中的差异变量DiffExprs
                 * 2. 寻找DiffExprs和input之间的关系
                 *      =》a. 获取调用图
                 *        b. 分析终点入口参数和被修改位置之间的关系?
                 *        c. ???
                 */
                for (Difference difference: differences) {
                    List<String> pathConditions = dependencyAnalysis(difference.getDiffExprInBuggy(), paths);
                }
            }
        }
        return map;
    }

    private static List<ExecutionPathInMth> pathSolver(ProjectPreparation projectPreparation, String tracePath) {
        List<String> trace = FileUtils.readEachLine(tracePath);
        List<ExecutionPathInMth> paths = new ArrayList<>();
        String lastMth = "";
        ExecutionPathInMth lastPath = null;
        for (String line : trace) {
            String clz = line.split("#")[0];//todo number identifier, should be transformed to correspond qualifiedName
            clz = clz.replace(".", File.separator);
            if (clz.contains("$")) {
                clz = clz.substring(0, clz.indexOf("$"));
            }
            String clzPath = (clz.contains("Test") ? projectPreparation.srcTestDir : projectPreparation.srcJavaDir) + File.separator + clz + ".java";
            int lineno = Integer.parseInt(line.split(":")[1]);
            if (lineno < 0)
                continue;
            CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(clzPath);
            CallableDeclaration methodDeclaration = TransformHelper.ASTExtractor.extractMethodByLine(compilationUnit, lineno);
            if (methodDeclaration == null)
                continue;
            if (!lastMth.equals(methodDeclaration.toString())) {
                lastMth = methodDeclaration.toString();
                ExecutionPathInMth executionPathInMth = new ExecutionPathInMth(methodDeclaration);
                lastPath = executionPathInMth;
                paths.add(lastPath);
            }
            lastPath.addLine(lineno);
        }
        return paths;
    }

    private static List<ExecutionPathInMth> pathSolverForSlicer4J(ProjectPreparation projectPreparation, String slicePath) {
        List<String> slices = FileUtils.readEachLine(slicePath);
        List<ExecutionPathInMth> paths = new ArrayList<>();
        String lastMth = "";
        ExecutionPathInMth lastPath = null;
        for (String line : slices) {
            String clz = line.split(":")[0];
            clz = clz.replace(".", File.separator);
            if (clz.contains("$")) {
                clz = clz.substring(0, clz.indexOf("$"));
            }
            String clzPath = (clz.contains("Test") ? projectPreparation.srcTestDir : projectPreparation.srcJavaDir) + File.separator + clz + ".java";
            int lineno = Integer.parseInt(line.split(":")[1]);
            if (lineno < 0)
                continue;
            CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(clzPath);
            CallableDeclaration methodDeclaration = TransformHelper.ASTExtractor.extractMethodByLine(compilationUnit, lineno);
            if (methodDeclaration == null)
                continue;
            if (!lastMth.equals(methodDeclaration.toString())) {
                lastMth = methodDeclaration.toString();
                ExecutionPathInMth executionPathInMth = new ExecutionPathInMth(methodDeclaration);
                lastPath = executionPathInMth;
                paths.add(lastPath);
            }
            lastPath.addLine(lineno);
        }
        return paths;
    }

    private static boolean containsInLines(List<Integer> lineno, Set<Node> nodes) {
        return nodes.stream().anyMatch(n -> n.getBegin().isPresent() && lineno.contains(n.getBegin().get().line));
    }

    //todo 这里传入的executionPath是buggy的！
    private static List<String> dependencyAnalysis(Pair<Set<Node>, Set<Node>> diffExprInBuggy, List<ExecutionPathInMth> path) {
        List<String> pathConditions = new ArrayList<>();
        PathCondition pathCondition = new PathCondition();
        if (!diffExprInBuggy.b.isEmpty()) {
            Set<Node> nodeInPat = diffExprInBuggy.b;
            nodeInPat.forEach(n -> n.accept(new VariableVisitor(), pathCondition));
            PathVisitor finalVisitor1 = new PathVisitor();
//            finalVisitor1.setEntry(true);

            //从最后一个执行到的函数往前找，切片入口是修改位置最早出现的地方。
            int startMth = path.size() - 1;
            while (startMth >= 0) {
                ExecutionPathInMth pathEntry = path.get(startMth);
                if (containsInLines(pathEntry.getLineno(), nodeInPat)) {
                    List<Node> nodes = pathEntry.getNodes();
                    finalVisitor1.setLineno(pathEntry.getLineno());
                    for (int j = nodes.size() - 1; j >= 0; j --) {
                        Node n = nodes.get(j);
                        n.accept(finalVisitor1, pathCondition);
                    }
                    break;
                }
                startMth --;
            }
            //倒着向前直到测试函数
//            finalVisitor1.setEntry(false);
            for (int i = startMth - 1; i >= 0; i --) {
                List<Node> nodes = path.get(i).getNodes();
                finalVisitor1.setLineno(path.get(i).getLineno());
                for (int j = nodes.size() - 1; j >= 0; j --) {
                    Node n = nodes.get(j);
                    n.accept(finalVisitor1, pathCondition);
                }
            }
            return pathConditions;
        }
        return null;
    }

    private static List<Skeleton> testGeneration(ProjectPreparation projectPreparation, Map<String, List<String>> testsByClazz,
                                                 List<Difference> differences) {
        List<Skeleton> mutateRes = new ArrayList<>();
        logger.info("Processing new Tests generation --------------------");
        for (Map.Entry<String, List<String>> entry :testsByClazz.entrySet()) {
            String clazzName = entry.getKey();
            String filePath = projectPreparation.srcTestDir + File.separator +
                    clazzName.replace(".", File.separator) + ".java";

            logger.info("For Test Class " + filePath + " --------------------");
            CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(filePath);
            logger.info("...Creating Skeleton");
            String[] s = clazzName.split(File.separator);
//            Skeleton skeleton = new Skeleton(filePath, compilationUnit, s[s.length - 1]);
            List<Input> inputs = getOriginalTestsInputs(compilationUnit, entry.getValue(), differences);
            Skeleton skeleton = TransformHelper.createASkeleton(filePath, s[s.length - 1], inputs);
            Map<String, MethodDeclaration> compilationUnitMap = TransformHelper.mutateTest(skeleton, inputs, differences);

            logger.info("Finishing one Test Class --------------------");
            mutateRes.add(skeleton);
        }
        logger.info("End new Tests generation --------------------");
        return mutateRes;
    }

    private static List<Input> getOriginalTestsInputs(CompilationUnit compilationUnit,
                                                      List<String> testMths, List<Difference> differences) {
        List<Input> inputs = new ArrayList<>();
        logger.info("Processing each test methods...");
        for (String testMth : testMths) {
            String[] split = testMth.split(":");
            String methodName = split[0];
            int lineNumber = split.length == 2 ? Integer.parseInt(split[1]) : 0;
            logger.info("Extracting test input for test " + methodName);
            Input input = TransformHelper.ASTExtractor.extractInput(compilationUnit, methodName, lineNumber);
            inputs.add(input);
        }
        return inputs;
    }

    private static boolean patchValidation(ProjectPreparation projectPreparation, List<Skeleton> mutateRes) {
        logger.info("Processing patches validation --------------------");
        boolean allCorrect = true;
        for (Patch patch: projectPreparation.patches) {
            boolean res = PatchHelper.validate(patch, mutateRes);
            allCorrect &= res;
        }
        logger.info("End patches validation --------------------");
        return allCorrect;
    }

    private static String getPatchDirByBug(String bugName, String patchesRootDir) {
        String[] split = bugName.split("_");
        String proj = split[0];
        String id = split[1];
        String patchDir = patchesRootDir + File.separator + proj + File.separator + proj + "_"+ id;
        return patchDir;
    }
}
