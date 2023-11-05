package root.generation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.AbstractMain;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.helper.MutatorHelper;
import root.generation.transformation.TransformHelper;
import root.util.CommandSummary;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestInputMutateForBugsMain extends AbstractMain {

    private static final Logger logger = LoggerFactory.getLogger(TestInputMutateForBugsMain.class);
    static TestInputMutateForBugsMain main = new TestInputMutateForBugsMain();

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
        complianceLevel = strings.get(8);
        cleaned = strings.get(9);
        cs.append("-proj", bugName.split("_")[0]);
        cs.append("-id", bugName.split("_")[1]);
        cs.append("-location", location + bugName + "_bug");
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

    public static void main0(String[] args) {
        String location = args[0]; // "/home/liumengjiao/Desktop/CI/bugs/";
        String info = args[1]; // "/home/liumengjiao/Desktop/CI/Benchmark_py/info/patches_inputs.csv";
        String patchesRootDir = args[2];
        List<List<String>> lists = FileUtils.readCsv(info, true);
        CommandSummary cs;
//        cs.read(args);
        for (List<String> strings :lists) {
            cs = setInputs(location, strings);
            String patchesDir = getPatchDirByBug(strings.get(0), patchesRootDir);
            cs.append("-patchesDir", patchesDir);
            boolean res = process(cs);
            if (!res) {
                continue;
            }
        }
    }

    public static void main(String[] args) {
        CommandSummary cs = new CommandSummary(args);
        boolean res = process(cs);
    }

    private static boolean process(CommandSummary cs) {
        ProjectPreparation projectPreparation = main.initialize(cs.flat());
        if (projectPreparation == null) {
            return false;
        }
        String testInfos = ConfigurationProperties.getProperty("testInfos");
        String[] tests = testInfos.split("#");
        Map<String, List<String>> testsByClazz = new HashMap<>();
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
        boolean allCorrect = true;
        for (Map.Entry<String, List<String>> entry :testsByClazz.entrySet()) {
            String clazzName = entry.getKey();
            String filePath = projectPreparation.srcTestDir + File.separator +
                    clazzName + ".java";
            String absolutePath = new File(filePath).getAbsolutePath();
            CompilationUnit compilationUnit = TransformHelper.inputExtractor.getCompilationUnit(absolutePath);
            Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, clazzName);
            List<Input> inputs = new ArrayList<>();
            for (String testMths : entry.getValue()) {
                String[] split = testMths.split(":");
                String methodName = split[0];
                int lineNumber = split.length == 2 ? Integer.parseInt(split[1]) : 0;
                Input input = TransformHelper.inputExtractor.extractInput(absolutePath, methodName, lineNumber);
                inputs.add(input);
            }
            Map<String, MethodDeclaration> compilationUnitMap = TransformHelper.mutateTest(skeleton, inputs, 10);
            boolean correct = TransformHelper.applyPatch(skeleton, compilationUnitMap);
            allCorrect &= correct;
        }
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
