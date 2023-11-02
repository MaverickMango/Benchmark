package root.generation.helper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import root.AbstractMain;
import root.util.CommandSummary;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.File;
import java.util.List;

public class PreparationTest {
    public static CommandSummary cs = new CommandSummary();
    public static AbstractMain main ;
    public static Preparation preparation;
    public static String filePath;// = "/home/liumengjiao/Desktop/CI/bugs/Closure_10_bug/test/com/google/javascript/jscomp/PeepholeFoldConstantsTest.java";
    public static String methodName;// = "testIssue821";
    public static int lineNumber;// = 582;

    @BeforeAll
    static void beforeAll() {
//        cs.append("-location", "/home/liumengjiao/Desktop/CI/bugs/Closure_10_bug");
//        cs.append("-srcJavaDir", "src");
//        cs.append("-srcTestDir", "test");
//        cs.append("-binJavaDir", "build/classes");
//        cs.append("-binTestDir", "build/test");
//        cs.append("-complianceLevel", "1.6");
        setInputs();
        main = new AbstractMain(cs.flat());
        preparation = main.getPreparation();

        String testInfos = ConfigurationProperties.getProperty("testInfos");
        String[] split = testInfos.split("#");
        if (split.length >= 1) {
            String triggerTest1 = split[0];
            String[] split1 = triggerTest1.split(":");
            filePath = PreparationTest.preparation.srcTestDir + File.separator +
                    split1[0].replaceAll("\\.", File.separator) + ".java";
            methodName = split1[1];
            if (split1.length == 3) {
                lineNumber = Integer.parseInt(split1[2]);
            }
        }
    }

    private static void setInputs() {
        String info = "/home/liumengjiao/Desktop/CI/Benchmark_py/info/patches_inputs.csv";
        List<List<String>> lists = FileUtils.readCsv(info, true);
        String location = "/home/liumengjiao/Desktop/CI/bugs/";
        String bugName, srcJavaDir, srcTestDir, binJavaDir, binTestDir, testInfos, projectCP, complianceLevel;
        List<String> strings = lists.get(38);
        bugName = strings.get(0);
        srcJavaDir = strings.get(1);
        srcTestDir = strings.get(2);
        binJavaDir = strings.get(3);
        binTestDir = strings.get(4);
        testInfos = strings.get(5);
        projectCP = strings.get(6);
        cs.append("-proj", bugName.split("_")[0]);
        cs.append("-id", bugName.split("_")[1]);
        cs.append("-location", location + bugName + "_bug");
        cs.append("-srcJavaDir", srcJavaDir);
        cs.append("-srcTestDir", srcTestDir);
        cs.append("-binJavaDir", binJavaDir);
        cs.append("-binTestDir", binTestDir);
        cs.append("-testInfos", testInfos);
        cs.append("-dependencies", projectCP);
//        cs.append("-complianceLevel", "1.6");
    }

}