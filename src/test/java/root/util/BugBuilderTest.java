package root.util;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import root.entity.benchmarks.Defects4JBug;
import root.entity.ci.CIBug;
import root.entity.ci.FailedTest;
import root.entity.ci.PatchDiff;

import java.io.File;
import java.util.List;
import java.util.Map;

class BugBuilderTest {

    CIBug ciBug = new Defects4JBug("Math", "105", "../bugs/Math_105_buggy");

    @BeforeEach
    void setUp() {
        ciBug.setBugName(((Defects4JBug) ciBug).getProj() + "_" + ((Defects4JBug) ciBug).getId());
        ciBug.setDerive("defects4j");
        ciBug.setOriginalFixingCommit("fc21b26f84312e4f75e8b144238618c73a8b091f");
        ciBug.setInducingCommit("2b57d6595c2c9095dc09bd42ba40fe1c69df0735");
        ((Defects4JBug) ciBug).setOriginalCommit("edbc57991142918547aa64e921408d1883cd670c");
        ((Defects4JBug) ciBug).setBuggyCommit("ab1b9500fd4b6898757e9c74dc2eeae692b25146");
    }

    @Test
    void buildABug() {
        BugBuilder.buildCIBug(ciBug, "data/changesInfo/", true);
        FileUtils.writeToFile(FileUtils.jsonFormatter(FileUtils.bean2Json(ciBug)),
                ((Defects4JBug) ciBug).getDataDir() + ciBug.getBugName() + "/original_fixing_info.json", false);
    }

    @Test
    void extractTests() {
        //ConfigurationProperties.getProperty("defects4j") + "/framework/projects/"
        //                + ((Defects4JBug) defects4JBug).getProj() + "/trigger_tests/" + ((Defects4JBug) defects4JBug).getId()
        List<FailedTest> failedTests = BugBuilder.extractFailedTests("/home/liumengjiao/Desktop/defects4j/framework/projects/Time/trigger_tests/11");
        System.out.println(FileUtils.bean2Json(failedTests.get(0)));
    }

    @Ignore
    void javap() {
//        ciBug = FileUtils.json2Bean(FileUtils.readFileByLines("data/changesInfo/" + ciBug.getBugName() + "/original_fixing_info.json"),
//                CIBug.class);
        String d4JFix = ((Defects4JBug) ciBug).getD4JFix();
//        boolean res = ((Defects4JBug) ciBug).switchAndCompile("f");
        String d4jinfoDir = ConfigurationProperties.getProperty("d4jinfo");
        if (d4jinfoDir != null ){
            d4jinfoDir += ((Defects4JBug) ciBug).getProj().toLowerCase() + File.separator + ((Defects4JBug) ciBug).getId() + ".txt";
        }
        List<String> path = FileUtils.readEachLine(d4jinfoDir);
        assert path.size() == 4;
        Map<String, String> properties = ((Defects4JBug) ciBug).getProperties("/defects4j.build.properties");
        String s = properties.get("src.dir");
        List<PatchDiff> fixingChanges = ciBug.getFixingChanges();
        for (PatchDiff diff :fixingChanges) {
        }
    }
}