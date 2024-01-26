package root;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.diff.DiffExtractor;
import root.diff.RefactoringMiner;
import root.generation.ProjectPreparation;
import root.generation.entity.Input;
import root.generation.entity.Patch;
import root.generation.entity.Skeleton;
import root.generation.transformation.InputTransformer;
import root.generation.transformation.TransformHelper;
import root.util.*;

import java.io.File;
import java.util.*;

public class TestMain extends AbstractMain {

    private static final Logger logger = LoggerFactory.getLogger(TestMain.class);
    static TestMain main = new TestMain();

    private static CommandSummary setInputs(String location, List<String> strings) {
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
        logger.info("Start Initialization.");
        ProjectPreparation projectPreparation = main.initialize(cs.flat());
        if (projectPreparation == null) {
            return false;
        }
        long initSeconds = TimeUtil.deltaInSeconds(bornTime);

        logger.info("Start diff extraction.");
        //todo add diffExtraction process
        diffExtraction(projectPreparation);

        logger.info("Start test generation.");
        String[] tests = ConfigurationProperties.getProperty("testInfos").split("#");
        Map<String, List<String>> testsByClazz = getTestInfos(tests);
        List<Skeleton> mutateRes = testMutation(projectPreparation, testsByClazz);

        logger.info("Start patch validation.");
        boolean allCorrect = patchValidation(projectPreparation, mutateRes);

        long totalSeconds = TimeUtil.deltaInSeconds(bornTime);
        logger.info("Finish with total running time: " + totalSeconds + "s");
        return allCorrect;
    }

    private static void diffExtraction(ProjectPreparation projectPreparation) {
        RefactoringMiner refactoringMiner = new RefactoringMiner();
        DiffExtractor diffExtractor = new DiffExtractor();
        String location = ConfigurationProperties.getProperty("location");
        List<Patch> patches = projectPreparation.patches;
        for (Patch patch :patches) {
            String bugPath = patch.getPatchAbsPath().replace(patch.getPathFromRoot(), location);
//            diffExtractor.diff(bugPath, patch.getPatchAbsPath());
            Set<ASTDiff> astDiffs = refactoringMiner.diffAtDirectories(new File(bugPath), new File(patch.getPatchAbsPath()));
            for (ASTDiff diff :astDiffs) {
                diff.getMultiMappings();
            }
        }
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

    private static List<Skeleton> testMutation(ProjectPreparation projectPreparation, Map<String, List<String>> testsByClazz) {
        List<Skeleton> mutateRes = new ArrayList<>();
        logger.info("Processing new Test generating --------------------");
        for (Map.Entry<String, List<String>> entry :testsByClazz.entrySet()) {
            String clazzName = entry.getKey();
            String filePath = projectPreparation.srcTestDir + File.separator +
                    clazzName + ".java";
            String absolutePath = new File(filePath).getAbsolutePath();
            logger.info("For Test Class " + absolutePath + " --------------------");
            CompilationUnit compilationUnit = TransformHelper.inputExtractor.getCompilationUnit(absolutePath);
            logger.info("...Creating Skeleton");
            String[] s = clazzName.split(File.separator);
            Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, s[s.length - 1]);
            List<Input> inputs = new ArrayList<>();
            logger.info("Processing each test methods...");
            for (String testMths : entry.getValue()) {
                String[] split = testMths.split(":");
                String methodName = split[0];
                int lineNumber = split.length == 2 ? Integer.parseInt(split[1]) : 0;
                logger.info("Extracting test input for test " + methodName);
                Input input = TransformHelper.inputExtractor.extractInput(compilationUnit, methodName, lineNumber);
                inputs.add(input);
            }
            Map<String, MethodDeclaration> compilationUnitMap = TransformHelper.mutateTest(skeleton, inputs, 10);
            logger.info("Finishing one Test Class --------------------");
            mutateRes.add(skeleton);
        }
        logger.info("End new Test generating --------------------");
        return mutateRes;
    }

    private static boolean patchValidation(ProjectPreparation projectPreparation, List<Skeleton> mutateRes) {
        logger.info("Processing patches validating --------------------");
        boolean allCorrect = true;
        for (Patch patch: projectPreparation.patches) {
            boolean res = PatchHelper.validate(patch, mutateRes);
            allCorrect &= res;
        }
        logger.info("End patches validating --------------------");
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
