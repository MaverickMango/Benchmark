package root.util;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import root.bean.benchmarks.Defects4JBug;
import root.bean.ci.CIBug;
import root.bean.ci.PatchDiff;

import java.io.File;
import java.util.List;
import java.util.Map;

class BugBuilderTest {

    CIBug ciBug = new Defects4JBug("Time", "1", "../bugs/Time_1_buggy");

    @BeforeEach
    void setUp() {
        ciBug.setBugName(((Defects4JBug) ciBug).getProj() + "_" + ((Defects4JBug) ciBug).getId());
        ciBug.setDerive("defects4j");
        ciBug.setOriginalFixingCommit("9a62b06be5d0df8e833ff8583398cca386608cac");
        ciBug.setInducingCommit("8612f9e5b88c1bea933ef9ab1e431f5db3006b48");
        ((Defects4JBug) ciBug).setOriginalCommit("8d109fe1a999a11b4557536dd96f9210460a5936");
        ((Defects4JBug) ciBug).setBuggyCommit("8612f9e5b88c1bea933ef9ab1e431f5db3006b48");
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
        BugBuilder.extractFailedTests("data/changesInfo/" + ciBug.getBugName() + "/properties/failing_tests/inducing");
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