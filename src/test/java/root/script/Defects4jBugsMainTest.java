package root.script;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import root.analysis.StringFilter;
import root.entities.otherdataset.BugFixCommit;
import root.entities.benchmarks.Defects4JBug;
import org.eclipse.jgit.lib.Repository;
import org.junit.Ignore;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Defects4jBugsMainTest implements GitAccess {

    Defects4JBug defects4JBug = new Defects4JBug("Lang", "4", "data/bugs/Lang_4_buggy");

    @Ignore
    public void test() {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        List<String> failed = new ArrayList<>();
        for (List<String> bug :d4jinfos) {
            if (Integer.parseInt(bug.get(0)) <= 90)
                continue;
            String bugName = bug.get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bugFixingCommit = bug.get(3);
            String bugInduingCommit = bug.get(5);
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, "../bugs/" + bugName);
            Repository repository = defects4JBug.getGitRepository("b");
            String patches = "data/changesInfo/" + proj + "_" + id + "/patches/";
            String modifiedClasses = "data/changesInfo/" + proj + "_" + id + "/properties/modified_classes/";
            String mappingFile = "data/changesInfo/" + proj + "_" + id + "/properties/mappings/b2o";
            String srcClasses = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/modified_classes/" + id + ".src";
            String srcPatch = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/patches/" + id + ".src.patch";
            String testPatch = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/patches/" + id + ".test.patch";
            String testMethods = "/home/liumengjiao/Desktop/vbaprinfo/d4j_bug_info/failed_tests/" + proj.toLowerCase() + "/" + id + ".txt";
//            FileUtils.copy(new File(srcPatch), new File(patches));
//            FileUtils.copy(new File(testPatch), new File(patches));
            try {
                List<List<String>> b2oSrc = gitAccess.getF2i(mappingFile, FileUtils.readEachLine(srcClasses));
                List<List<String>> f2iTest = gitAccess.getF2i(mappingFile, FileUtils.readEachLine(testMethods));
                boolean checkoutf = gitAccess.checkoutf(defects4JBug.getWorkingDir(), bug.get(6));
                for (List<String> b2i : b2oSrc) {
                    FileUtils.copy(new File(defects4JBug.getWorkingDir() + "/" + b2i.get(2)), new File(modifiedClasses + "original/" + b2i.get(2)));
                }
                for (List<String> b2i : f2iTest) {
                    FileUtils.copy(new File(defects4JBug.getWorkingDir() + "/" + b2i.get(2)), new File(modifiedClasses + "original/" + b2i.get(2)));
                }
//                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "diff -r -u original/ buggy/ > " + "../../patches/o2b.diff 2>&1"}, modifiedClasses, 300, null);
            } catch (Exception e) {
                failed.add(bugName);
                System.err.println(bugName);
                e.printStackTrace();
            }
        }
    }

    @Ignore
    public void testMain() throws Exception {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        Set<String> projs = d4jinfos.stream().map(o -> o.get(2).split("_")[0]).collect(Collectors.toSet());
        List<String> failed = new ArrayList<>();
        for (String proj :projs) {
            if (proj.equals("Closure"))
                continue;
            for (List<String> bug : d4jinfos.stream().filter(o -> o.get(2).split("_")[0].equals(proj)).collect(Collectors.toList())) {
                String bugName = bug.get(2);
                String id = bugName.split("_")[1];
                if (proj.equals("Math") && Integer.parseInt(id) < 45)
                    continue;
                String bugInduingCommit = bug.get(5);
                String bugOriginal = bug.get(6);
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "data/bugs/" + bugName);
                Repository repo = defects4JBug.getGitRepository("b");
                boolean res = false;
                res = defects4JBug.switchAndTest(repo, bugOriginal, "original");
                if (!res) {
                    failed.add(bugName);
                }
            }
        }
        System.out.println(failed);
    }

    @Ignore
    public void testInsertTest() throws Exception {
        defects4JBug.setProj("Math");
        defects4JBug.setId("50");
        defects4JBug.setWorkingDir("data/bugs/Math_50_buggy_test");
        Repository repo = defects4JBug.getGitRepository("b");
//        defects4JBug.switch2TargetCommit(repo, "f6fddf94c74502469c013ed94599bbd7fa2d89e7", "original", "CI_ORIGINAL_POST_COMPILABLE");
//        gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), "c0b655ace5665c0cd32e3f5e5b46edad4d223125"
//                , "711d6b94a120d413e9d8bd21bb26ec7d0aeecc39"
//                , "tmp/changesInfo/Math_2/properties/mappings/f2i");
        ArrayList<String> strings = new ArrayList<>();
        strings.add("org.apache.commons.math.analysis.solvers.RegulaFalsiSolverTest::testIssue631");
        defects4JBug.addTest(repo, "data/changesInfo/Math_50/properties/mappings/f2i", strings, defects4JBug.getFixingCommit(), "2f066a5b2d2fe8a00a251a3220b0d52446fe392d");
//        String[] f2i = gitAccess.getF2i("tmp/changesInfo/Math_50/properties/mappings/f2i", "org.apache.commons.math.analysis.solvers.RegulaFalsiSolverTest");
//        gitAccess.checkoutf(defects4JBug.getWorkingDir(), "39cf5e69259d7560d50553caf028f9229b721013");
//        ASTManipulator astManipulator = new ASTManipulator(8);
//        List<ImportDeclaration> importDeclarations = new ArrayList<>();
//        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
//        ASTNode triggerTest = astManipulator.extractTest(FileUtils.readFileByChars(defects4JBug.getWorkingDir() + "/" + f2i[1]), "testIssue631"
//                                , importDeclarations, methodDeclarations);
//        gitAccess.checkoutf(defects4JBug.getWorkingDir(), "2f066a5b2d2fe8a00a251a3220b0d52446fe392d");
//        String s = astManipulator.insertTest(FileUtils.readFileByChars(defects4JBug.getWorkingDir() + "/" + f2i[2]), triggerTest
//                                , "tmp/changesInfo/Math_50/properties/mappings/f2i", importDeclarations, methodDeclarations);
//        FileUtils.writeToFile(s, defects4JBug.getWorkingDir() + "/" + f2i[2], false);
    }

    @Ignore
    public void testtest() throws IOException {
        defects4JBug.setProj("Closure");
        defects4JBug.setId("19");
        defects4JBug.setWorkingDir("data/bugs/Closure_19_buggy");
        Repository repository = defects4JBug.getGitRepository("b");
        List<Ref> refsByPrefix = repository.getRefDatabase().getRefsByPrefix(Constants.R_TAGS);
        List<Ref> ci_inducing_compilable = refsByPrefix.stream().filter(o -> o.getName().contains("CI_INDUCING_COMPILABLE")).collect(Collectors.toList());
//        boolean res = defects4JBug.test();
        System.out.println(ci_inducing_compilable.isEmpty());
    }


    @Ignore
    public void testDiff() throws Exception {
        String bugName = "Math_45";
        String bugInduingCommit = "eb1b2cfefa07149f078a81c8fb30bb826062b7c5";
        String workingDir = "data/bugs/" + bugName + "_buggy";
        Defects4JBug defects4JBug = new Defects4JBug("Math", "45",  workingDir);
        Repository repository = defects4JBug.getGitRepository("b");
        String bugFixingCommit = "bc4e9db01c2a03062965fa4bac65782376ab2287";
        assert repository != null;
        String fixingDiff = gitAccess.diff(repository, bugFixingCommit);
        String inducingDiff = gitAccess.diff(repository, bugInduingCommit);
        String fixingDiffDir = "data/changesInfo/" + bugName + "/patches/fixing.diff";
        String inducingDiffDir = "data/changesInfo/" + bugName + "/patches/inducing.diff";
        String changesInfoDir = "data/changesInfo/" + bugName + "/info.txt";
        if (fixingDiff != null && inducingDiff != null) {
            BugFixCommit bugFixCommit = gitAccess.getBugFixCommit(bugName, "0",
                    repository, bugInduingCommit, bugFixingCommit);
            FileUtils.writeToFile(bugFixCommit.toString(), changesInfoDir, false);
            FileUtils.writeToFile(fixingDiff, fixingDiffDir, false);
            FileUtils.writeToFile(inducingDiff, inducingDiffDir, false);
        }
    }

    @Ignore
    public void testFilter() throws Exception {
        String bugName = "Math_45";
        String workingDir = "data/bugs/" + bugName + "_buggy";
        Defects4JBug defects4JBug = new Defects4JBug("Math", "45",  workingDir);
        Repository repository = defects4JBug.getGitRepository("b");
        String bugFixingCommit = "bc4e9db01c2a03062965fa4bac65782376ab2287";
        StringFilter filter = new StringFilter(StringFilter.MATCHES);
        filter.addPattern(".*Test.java$");
        String clz = "org.apache.commons.math.linear.OpenMapRealMatrix";
        StringBuilder pattern = new StringBuilder("^(?!.*(?:");
        String s = clz.replaceAll("[.]", File.separator);
        pattern.append(s).append("|");
        pattern.replace(pattern.length() - 1, pattern.length(), ")");
        pattern.append("\\.java$).*");
        filter.addPattern(pattern.toString());
        String nextCommit = gitAccess.getNextCommit(repository, bugFixingCommit, false);
        String diff = gitAccess.diffWithFilter(defects4JBug.getGitRepository("buggy"), bugFixingCommit, nextCommit, filter);
    }

    @Ignore
    public void testTest() {
        boolean res = defects4JBug.test();
        System.out.println(res);
    }
}