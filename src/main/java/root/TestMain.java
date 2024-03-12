package root;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.slicer.Slicer;
import root.diff.DiffExtractor;
import root.entities.*;
import root.generation.entities.Input;
import root.generation.entities.Skeleton;
import root.generation.transformation.TransformHelper;
import root.util.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Path path = Paths.get(location).toAbsolutePath();
        location = path.resolve(bugName.toLowerCase() + "_buggy").normalize().toString();
        cs.append("-proj", bugName.split("_")[0]);
        cs.append("-id", bugName.split("_")[1]);
        cs.append("-location", location);
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
        for (int i = 0; i < lists.size(); i ++) {
            List<String> strings  = lists.get(i);
            cs = setInputs(location, strings);
            String patchesDir = getPatchDirByBug(strings.get(0), patchesRootDir);
            cs.append("-patchesDir", patchesDir);
            cs.append("-sliceRoot", sliceRoot);
            boolean res = executeMainProcess(cs);
        }
    }

    private static boolean executeMainProcess(CommandSummary cs) {
        TestMain main = new TestMain();
        logger.info("---------Start Initialization----------");
        ProjectPreparation projectPreparation = main.initialize(cs.flat());
        if (projectPreparation == null) {
            return false;
        }
        String[] tests = ConfigurationProperties.getProperty("testInfos").split("#");
        Map<String, List<String>> testsByClazz = getTestInfos(tests);
        long seconds = TimeUtil.deltaInSeconds(main.bornTime);
        Stats.getCurrentStats().addGeneralStat(Stats.General.INITIALIZATION_TIME, seconds);
//        logger.info("Initialization end with time cost " + seconds + "s");

        logger.info("----------Start diff extraction----------");
        /*
         * 1. 提取differences中的差异变量DiffExprs
         * 2. 寻找DiffExprs和input之间的关系
         *      =》a. <s>获取调用图</s> 获取失败测试的trace
         *        b. <s>静态分析每个函数内部的依赖关系</s> 根据trace进行切片
         *        c. 在切片的同时构造其依赖的条件表达式和变量传播关系
         */
        Date startDate = new Date();
        List<Difference> differences = diffExtraction(projectPreparation);
        seconds = TimeUtil.deltaInSeconds(startDate);
        Stats.getCurrentStats().addGeneralStat(Stats.General.DIFF_TIME, seconds);
//        logger.info("Diff extraction end with time cost " + seconds + "s");

        logger.info("----------Start slicing extraction----------");
        List<PathFlow> pathFlows = constructRestraints(projectPreparation, differences);

        boolean allCorrect = false;
        if (allCorrect) {
            logger.info("----------Start test generation----------");
            startDate = new Date();
            List<Skeleton> mutateRes = testGeneration(projectPreparation, testsByClazz, differences);
            seconds = TimeUtil.deltaInSeconds(startDate);
            Stats.getCurrentStats().addGeneralStat(Stats.General.GENERATION_TIME, seconds);
//        logger.info("Test generation end with time cost " + seconds + "s");

            logger.info("----------Start patch validation----------");
            startDate = new Date();
            allCorrect = patchValidation(projectPreparation, mutateRes);
            seconds = TimeUtil.deltaInSeconds(startDate);
            Stats.getCurrentStats().addGeneralStat(Stats.General.VALIDATION_TIME, seconds);
//        logger.info("Patch validation end with time cost " + seconds + "s");
        }

        long totalSeconds = TimeUtil.deltaInSeconds(main.bornTime);
        Stats.getCurrentStats().addGeneralStat(Stats.General.TOTAL_TIME, seconds);
        logger.info("Finish with total running time: " + totalSeconds + "s");

        String resultOutput = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + projectPreparation.bug.getBugName() + File.separator + "stats";
        FileUtils.writeToFile(Stats.getCurrentStats().toString(), resultOutput, false);
        return allCorrect;
    }

    private static Map<String, List<String>> getTestInfos(String[] tests) {
        Map<String, List<String>> testsByClazz = new HashMap<>();
        logger.info("...Split test one by one.");
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
        return testsByClazz;
    }

    private static List<Difference> diffExtraction(ProjectPreparation projectPreparation) {
        DiffExtractor diffExtractor = new DiffExtractor();
        List<Difference> differences = new ArrayList<>();
        List<Patch> patches = projectPreparation.patches;
        for (Patch patch :patches) {
            Difference difference = diffExtractor.getDifferenceForPatch(patch);
            differences.add(difference);
        }
        return differences;
    }


    private static List<PathFlow> constructRestraints(ProjectPreparation projectPreparation, List<Difference> differences) {
        List<PathFlow> map = new ArrayList<>();
        Slicer slicer = projectPreparation.slicer;
        List<ExecutionPathInMth> paths = slicer.traceParser();//每个覆盖函数对应的执行路径
        for (Difference difference: differences) {
            PathFlow pathFlow = slicer.dependencyAnalysis(difference.getDiffExprInBuggy(), paths);
            map.add(pathFlow);

            Stats.getCurrentStats().addPatchStat(difference.getPatch().getName(), Stats.Patch.PATH_FLOW, pathFlow);
        }
        return map;
    }

    private static List<Skeleton> testGeneration(ProjectPreparation projectPreparation, Map<String, List<String>> testsByClazz,
                                                 List<Difference> differences) {
        List<Skeleton> mutateRes = new ArrayList<>();
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
