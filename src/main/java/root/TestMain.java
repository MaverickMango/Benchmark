package root;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.diff.DiffExtractor;
import root.entities.Difference;
import root.entities.Stats;
import root.generation.entities.Input;
import root.entities.Patch;
import root.generation.entities.Skeleton;
import root.generation.transformation.TransformHelper;
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
        List<List<String>> lists = FileUtils.readCsv(info, true);
        CommandSummary cs;
        for (int i = 25; i < lists.size(); i ++) {
            List<String> strings  = lists.get(i);
            cs = setInputs(location, strings);
            String patchesDir = getPatchDirByBug(strings.get(0), patchesRootDir);
            cs.append("-patchesDir", patchesDir);
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
        return allCorrect;
    }

    private static Map<String, List<String>> getTestInfos(String[] tests) {
        Map<String, List<String>> testsByClazz = new HashMap<>();
        logger.info("Preprocessing --------------------");
        logger.info("Split test one by one...");
        for (String triggerTest : tests) {
            String[] split1 = triggerTest.split(":");
            String clazzName = split1[0].replaceAll("\\.", File.separator);
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

    private static List<Skeleton> testGeneration(ProjectPreparation projectPreparation, Map<String, List<String>> testsByClazz,
                                                 List<Difference> differences) {
        List<Skeleton> mutateRes = new ArrayList<>();
        logger.info("Processing new Tests generation --------------------");
        for (Map.Entry<String, List<String>> entry :testsByClazz.entrySet()) {
            String clazzName = entry.getKey();
            String filePath = projectPreparation.srcTestDir + File.separator +
                    clazzName + ".java";
            String absolutePath = new File(filePath).getAbsolutePath();
            logger.info("For Test Class " + absolutePath + " --------------------");
            CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(absolutePath);
            logger.info("...Creating Skeleton");
            String[] s = clazzName.split(File.separator);
            Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, s[s.length - 1]);

            List<Input> inputs = getOriginalTestsInputs(skeleton, compilationUnit, entry.getValue());
            Map<String, MethodDeclaration> compilationUnitMap = TransformHelper.mutateTest(skeleton, inputs, differences);

            logger.info("Finishing one Test Class --------------------");
            mutateRes.add(skeleton);
        }
        logger.info("End new Tests generation --------------------");
        return mutateRes;
    }

    private static List<Input> getOriginalTestsInputs(Skeleton skeleton, CompilationUnit compilationUnit,
                                                      List<String> testMths) {
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
