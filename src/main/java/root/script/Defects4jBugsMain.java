package root.script;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entities.ci.BugWithHistory;
import root.entities.otherdataset.BugFixCommit;
import root.entities.benchmarks.Defects4JBug;
import org.eclipse.jgit.lib.Repository;
import root.util.BugBuilder;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Defects4jBugsMain implements GitAccess {

    private static final Logger logger = LoggerFactory.getLogger(Defects4jBugsMain.class);

    public static void main(String[] args) {//jsonAnalyzer
        String bugInfo = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(bugInfo, true);
        List<String> addClasses = new ArrayList<>();
        for (int i = 0; i < d4jinfos.size(); i ++) {
            String bugName = d4jinfos.get(i).get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bug_tag = proj + "_" + id;
            String jsonFile = "data/changesInfo/" + bug_tag + "/origianl_fixing_info.json";
            String inducingDir = "data/changesInfo/" + bug_tag + "/cleaned/inducing";
            if (FileUtils.notExists(jsonFile)) {
                continue;
            }
            String json = FileUtils.readFileByLines(jsonFile);
            addClasses.add(json);
//            String workingDir = "../bugs/" + bugName;
//            String bugInducingCommit = d4jinfos.get(i).get(5);
//            String bugFixingCommit = d4jinfos.get(i).get(3);
//            CIBug ciBug = FileUtils.json2Bean(json, CIBug.class);
//            if (ciBug.getInducingChanges().isEmpty())
//                continue;
//            Actions inducingType = ciBug.getInducingType();
//            if (inducingType.getAddClasses().getNum() != 0) {
//                addClasses.add(bugName);
//            }
//            if (inducingType.getAddClasses().getNum() != 0) {
//                addClasses.add(bugName);
//            }
        }
        FileUtils.writeToFile(FileUtils.jsonFormatter(addClasses.toString()), "data/changesInfo/info.json", false);
    }

    public static void functionExtractor(String[] args) {//
        String bugInfo = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(bugInfo, false);
        for (int i = 0; i < d4jinfos.size(); i ++) {
            String bugName = d4jinfos.get(i).get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bug_tag = proj + "_" + id;
            String fixingDir = "data/changesInfo/" + bug_tag + "/cleaned/fixing";
            String inducingDir = "data/changesInfo/" + bug_tag + "/cleaned/inducing";
//            if (FileUtils.notExists(inducingDir)) {
//                continue;
//            }
            String workingDir = "../bugs/" + bugName;
            String bugInducingCommit = d4jinfos.get(i).get(5);
            String bugFixingCommit = d4jinfos.get(i).get(3);
            BugWithHistory defects4JBug = new Defects4JBug(proj, id,  workingDir);
            defects4JBug.setBugName(bug_tag);
            defects4JBug.setDerive("defects4j");
            defects4JBug.setOriginalFixingCommit(bugFixingCommit);
            defects4JBug.setInducingCommit(bugInducingCommit);
            ((Defects4JBug)defects4JBug).setOriginalCommit(d4jinfos.get(i).get(6));
            ((Defects4JBug)defects4JBug).setBuggyCommit(d4jinfos.get(i).get(4));

            String buildFileDir = "data/changesInfo/" + bug_tag + "/buildfiles";
            defects4JBug.setBuildfilesChanged(!FileUtils.notExists(buildFileDir));
            BugBuilder.buildCIBug(defects4JBug, "data/changesInfo/", true);
            FileUtils.writeToFile(FileUtils.bean2Json(defects4JBug),
                    ((Defects4JBug) defects4JBug).getDataDir() + defects4JBug.getBugName() + "/origianl_fixing_info.json", false);
        }
    }

    /**
     *
     * @param args [0]: proj_bugs.csv [1]: target bug cloned directory [2]: bugfixinfo.csv [3]: path2changesInfo/
     */
    public static void getFixing(String[] args) {
        List<String> proj_bugs = FileUtils.readEachLine(args[0]);
        List<List<String>> bugFixInfos = FileUtils.readCsv(args[2], true);
        Set<String> collect = bugFixInfos.stream().map(i -> i.get(2)).collect(Collectors.toSet());
        for (String proj_bug: proj_bugs) {
            String proj = proj_bug.split(":")[0];
            if (!proj.equals("Codec"))
                continue;
            String[] bugs = proj_bug.split(":")[1].split(",");
            for (String id :bugs) {
//                if (!FileUtils.notExists(args[3] + proj + "_" + id)){
//                    continue;
//                }
                String bugName = proj + "_" + id + "_buggy";
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, args[1] + bugName);
                String fixingCommit = defects4JBug.getFixingCommit();
//                String startCommit = fixingCommit;
//                if (collect.contains(bugName)) {
//                    List<List<String>> bugInfo = bugFixInfos.stream().filter(i -> i.get(2).equals(bugName)).collect(Collectors.toList());
//                    if (!bugInfo.isEmpty()) {
//                        startCommit = bugInfo.get(0).get(6);
//                    }
//                }
//                boolean res = defects4JBug.findInducingCommit(fixingCommit, startCommit);
                FileUtils.writeToFile(fixingCommit + "\n", "data/codec-d4j-fixing.txt", true);
                logger.info("Finished processing " + bugName + "...");
            }
        }
    }

    /**
     *
     * @param args [0]:failing_tests store directory
     */
    public static void findingInducing(String[] args) {
        String filePath = args[1];//"src/test/resources/BugFixInfo_total.csv"
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, false);
        List<String> succeed = new ArrayList<>();
        Map<String, String[]> filter = new HashMap<>();
        logger.info("Starting find original commit...");
        //original wrong
        filter.put("Closure", new String[]{"19","52","59","60","66","68","76","80","82","85","90","99","118","131"});
        filter.put("Math", new String[]{"12","13","26","45","46","48","60","74","88"});
        filter.put("Time", new String[]{"23"});
        for (List<String> bug :d4jinfos) {
            String bugName = bug.get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            if (!filter.containsKey(proj) || !Arrays.asList(filter.get(proj)).contains(id))
                continue;
            logger.info("Starting process " + bugName + "...");
            String bugFixingCommit = bug.get(3);
            String bugInduingCommit = bug.get(5);
            String fakeInducing = "", fakeOriginal = "";
            boolean res = false;
            try {
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "data/bugs/" + bugName);
                Repository repository = defects4JBug.getGitRepository("b");
                gitAccess.checkoutf(defects4JBug.getWorkingDir(), bug.get(6));
//                if (!res) {
//                    logger.info("---------- " + bugName + " failed in initial test.");
//                    continue;
//                }
//                Map<String, String> properties = defects4JBug.getProperties("/defects4j.build.properties");
//                defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
//                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp -r ../../changesInfo/" + proj + "_" + id + "/cleaned/inducing/* ./"}, defects4JBug.getWorkingDir(), 300, null);
                List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repository, false);
                int i = 1;
                List<String> triggerTests = defects4JBug.getTriggerTests(ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/trigger_tests/" + id);
                for (; i < revsWalkOfAll.size(); i++) {
                    String commit = revsWalkOfAll.get(i).getName();
                    fakeInducing = revsWalkOfAll.get(i - 1).getName();
                    fakeOriginal = commit;
                    res = defects4JBug.switchAndTest(repository, commit, "original");
                    if (!res) {
                        logger.info("---------- " + bugName + " failed in continuous test.");
                        break;
                    }
                    List<String> failingTests = defects4JBug.getFailingTests("failing_tests");
//                    properties = defects4JBug.getProperties("/defects4j.build.properties");
//                    defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
                    FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp -r ../../changesInfo/" + proj + "_" + id + "/cleaned/inducing/* ./"}, defects4JBug.getWorkingDir(), 300, null);
                    if (FileUtils.notExists("data/changesInfo/" + proj + "_" + id + "/cleaned/test_script")) {
                        res = defects4JBug.test();
                    } else {
                        List<String> test_scripts = FileUtils.readEachLine("data/changesInfo/" + proj + "_" + id + "/cleaned/test_script");
                        for (String test_script :test_scripts) {
                            int r = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "timeout 300000 " + test_script}, defects4JBug.getWorkingDir(), 300, null);
                            res &= r == 0;
                        }
                    }
                    if (!res) {
                        logger.info("---------- " + bugName + " failed after changing tests.");
                        break;
                    }
                    List<String> failingTests_new = defects4JBug.getFailingTests("failing_tests");
                    if (failingTests.size() == failingTests_new.size() || defects4JBug.isAllTest1NotInTests2(triggerTests, failingTests_new)) {
                        logger.info("----------" + bugName + " successfully found original commit");
                        FileUtils.writeToFile(bugName + "," + fakeInducing + "," + fakeOriginal + "\n", args[0] + "/bug_original_commits", true);
                        break;
                    } else {
                        res = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
            }
            if (!res) {
                logger.info("----------" + bugName + " can not find original commit");
                FileUtils.writeToFile(bugName + "," + fakeInducing + "," + fakeOriginal + "\n", args[0] + "/bug_original_commits", true);
            }
            logger.info("Finished processing " + bugName + "...");
        }
        logger.info("Starting find inducing commit...");
        //inducing wrong
        filter.clear();
        filter.put("Closure", new String[]{"33","107","114","125","130","133"});
        filter.put("Math", new String[]{"14","23","24"});
        for (List<String> bug :d4jinfos) {
            String bugName = bug.get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            if (!filter.containsKey(proj) || !Arrays.asList(filter.get(proj)).contains(id))
                continue;
            logger.info("Starting process " + bugName + "...");
            String bugFixingCommit = bug.get(3);
            String bugInduingCommit = bug.get(5);
            boolean res = false;
            String fakeInducing = "", fakeOriginal = "";
            try {
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "data/bugs/" + bugName);
                Repository repository = defects4JBug.getGitRepository("b");
                gitAccess.checkoutf(defects4JBug.getWorkingDir(), bug.get(4));
//                if (!res) {
//                    logger.info("---------- " + bugName + " failed in initial test.");
//                    continue;
//                }
//                Map<String, String> properties = defects4JBug.getProperties("/defects4j.build.properties");
//                String failing_tests_path = defects4JBug.getWorkingDir() + "/failing_tests";
//                List<String> failing_tests = FileUtils.readEachLine(failing_tests_path);
//                defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
//                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp -r ../../changesInfo/" + proj + "_" + id + "/cleaned/inducing/ ./"}, defects4JBug.getWorkingDir(), 300, null);
                List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repository, false);
                List<RevCommit> collect = revsWalkOfAll.stream().filter(c -> c.getName().equals(bug.get(6))).collect(Collectors.toList());
                int endIdx = revsWalkOfAll.size() - 1;
                if (!collect.isEmpty()) {
                    endIdx = revsWalkOfAll.indexOf(collect.get(0)) - 1;
                }
                for (int i = endIdx; i > 0; i--) {
                    String commit = revsWalkOfAll.get(i).getName();
                    fakeOriginal = revsWalkOfAll.get(i + 1).getName();
                    fakeInducing = commit;
                    res = defects4JBug.switchAndTest(repository, commit, "inducing");
                    if (!res) {
                        logger.info("---------- " + bugName + " failed in continuous test.");
                        continue;
                    }
                    List<String> failingTests = defects4JBug.getFailingTests("failing_tests");
//                    properties = defects4JBug.getProperties("/defects4j.build.properties");
//                    defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
                    FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp -r ../../changesInfo/" + proj + "_" + id + "/cleaned/inducing/* ./"}, defects4JBug.getWorkingDir(), 300, null);
                    if (FileUtils.notExists("data/changesInfo/" + proj + "_" + id + "/cleaned/test_script")) {
                        res = defects4JBug.test();
                    } else {
                        List<String> test_scripts = FileUtils.readEachLine("data/changesInfo/" + proj + "_" + id + "/cleaned/test_script");
                        for (String test_script :test_scripts) {
                            int r = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", test_script}, defects4JBug.getWorkingDir(), 300, null);
                            res &= r == 0;
                        }
                    }
                    if (!res) {
                        logger.info("---------- " + bugName + " failed after changing tests.");
                        continue;
                    }
                    List<String> failingTests_new = defects4JBug.getFailingTests("failing_tests");
                    List<String> triggerTests = defects4JBug.getTriggerTests(ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/trigger_tests/" + id);
                    if (failingTests.size() != failingTests_new.size() || !defects4JBug.isAllTest1NotInTests2(triggerTests, failingTests_new)) {
                        logger.info("----------" + bugName + " successfully found inducing commit");
                        FileUtils.writeToFile(bugName + "," + fakeInducing + "," + fakeOriginal + "\n", args[0] + "/bug_inducing_commits", true);
                        break;
                    } else {
                        res = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
            }
            if (!res) {
                logger.info("----------" + bugName + " can not find inducing commit");
                FileUtils.writeToFile(bugName + "," + fakeInducing + "," + fakeOriginal + "\n", args[0] + "/bug_inducing_commits", true);
            }
            logger.info("Finished processing " + bugName + "...");
        }
    }


    public static void getsucceed(String[] args) {
        String filePath = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, false);
        Set<String> projs = d4jinfos.stream().map(o -> o.get(2).split("_")[0]).collect(Collectors.toSet());
        List<String> failed = new ArrayList<>();
        Map<String, String[]> filter = new HashMap<>();
        filter.put("Closure", new String[]{"2","62","65","74"});
        filter.put("Math", new String[]{"26","28", "53","60","75","74", "87", "88","89","94"});
        filter.put("Lang", new String[]{"4","65"});
        for (String proj :projs) {
            if (proj.equals("Time") || proj.equals("Closure"))
                continue;
            for (List<String> bug : d4jinfos.stream().filter(o -> o.get(2).split("_")[0].equals(proj)).collect(Collectors.toList())) {
                String bugName = bug.get(2);
                String id = bugName.split("_")[1];
                if (Arrays.asList(filter.get(proj)).contains(id))
                    continue;
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "data/bugs/" + bugName, bug.get(3), bug.get(4), bug.get(5), bug.get(6));
                Repository repo = defects4JBug.getGitRepository("b");
//                boolean res = defects4JBug.switchAndTag(repo, defects4JBug.getInducingCommit(), "inducing", "CI_INDUCING_COMPILABLE");
//                if (!res) {
//                    failed.add(bugName);
//                }
            }
        }
        System.out.println(failed.size());
        System.out.println(failed);
    }

    public static void getMappings(String[] args) {//
        String filePath = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, false);
        for (List<String> bug :d4jinfos) {
            if (Integer.parseInt(bug.get(0)) <= 90)
                continue;
            String bugName = bug.get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            //                if (Integer.parseInt(id) >= 11)
            //                    continue;
            String bugFixingCommit = bug.get(3);
            String bugInduingCommit = bug.get(5);
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, "../bugs/" + bugName);
            Repository repository = defects4JBug.getGitRepository("b");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bug.get(5), bug.get(6)
                    , "data/changesInfo/" + proj + "_" + id + "/properties/mappings/i2o");
        }
    }

    public static void switchAndTest(String[] args) {
        String filePath = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, false);
        Set<String> projs = d4jinfos.stream().map(o -> o.get(2).split("_")[0]).collect(Collectors.toSet());
        List<String> failed = new ArrayList<>();
        for (String proj :projs) {
//            if (!proj.equals("Lang"))
//                continue;
            for (List<String> bug : d4jinfos.stream().filter(o -> o.get(2).split("_")[0].equals(proj)).collect(Collectors.toList())) {
                String bugName = bug.get(2);
                String id = bugName.split("_")[1];
                String bugInduingCommit = bug.get(5);
                String bugOriginal = bug.get(6);
                String bug_tag = proj + "_" + id;
                String inducingDir = "data/changesInfo/" + bug_tag + "/cleaned/inducing";
                if (FileUtils.notExists(inducingDir)) {
                    continue;
                }
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "../bugs/" + bugName);
                Repository repo = defects4JBug.getGitRepository("b");
                boolean res = false;
                res = defects4JBug.switchAndTest(repo, bugInduingCommit, "inducing");
//                String all_tests = "tmp/changesInfo/" + proj + "_" + id + "/properties/all_tests/inducing";
//                if (!new File(all_tests).exists()) {
//                    res = defects4JBug.switchAndTest(repo, bugInduingCommit, "inducing");
//                } else
//                    res = true;
//                all_tests = "tmp/changesInfo/" + proj + "_" + id + "/properties/all_tests/original";
//                if (!new File(all_tests).exists()) {
//                    res &= defects4JBug.switchAndTest(repo, bugOriginal, "original");
//                }
                if (!res) {
                    failed.add(bugName);
                }
            }
        }
        System.out.println(failed);
    }

    public static void rewriteD4jInfo(String[] args) {//
        String filePath = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, false);
        Set<String> projs = d4jinfos.stream().map(o -> o.get(2).split("_")[0]).collect(Collectors.toSet());
        for (String proj :projs) {
            String activeBugs = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/active-bugs.csv";
            String commitDB = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/commit-db";
            List<List<String>> bugsinfo = FileUtils.readCsv(activeBugs, false);
            List<List<String>> newinfos = new ArrayList<>();
            if (bugsinfo.get(0).size() == 5) {
                List<String> tmp = new ArrayList<>(bugsinfo.remove(0));
                tmp.add(3, "revision.id.inducing");
                tmp.add(4, "revision.id.original");
                newinfos.add(0, tmp);
            }
            while (!bugsinfo.isEmpty()){
                List<String> tmp = new ArrayList<>(bugsinfo.remove(0));
                tmp.add(3, "00000000");
                tmp.add(4, "00000000");
                newinfos.add(tmp);
            }
            for (List<String> bug : d4jinfos.stream().filter(o -> o.get(2).split("_")[0].equals(proj)).collect(Collectors.toList())) {
                String bugName = bug.get(2);
                String id = bugName.split("_")[1];
                String bugInduingCommit = bug.get(5);
                String bugOriginal = bug.get(6);
                List<List<String>> collect = newinfos.stream().filter(o -> o.get(0).equals(id)).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    List<String> line = collect.get(0);
                    line.set(3, bugInduingCommit);
                    line.set(4, bugOriginal);
                }
            }
            FileUtils.writeCsv(newinfos, activeBugs + ".ci", false);
            FileUtils.writeCsv(newinfos, commitDB + ".ci", true);

            FileUtils.copy(new File(activeBugs), new File(activeBugs + ".bak"));
            FileUtils.copy(new File(commitDB), new File(commitDB + ".bak"));
            FileUtils.copy(new File(activeBugs + ".ci"), new File(activeBugs));
            FileUtils.copy(new File(commitDB + ".ci"), new File(commitDB));
        }
    }


    public static void getChangeInfo(String[] args) {//
        String filePath = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, false);
        for (List<String> bug : d4jinfos) {
            if (Integer.parseInt(bug.get(0)) <= 90)
                continue;
            String bugName = bug.get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bugInduingCommit = bug.get(5);
            String workingDir = "../bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            String bugFixingCommit = bug.get(3);
            String fileDir = defects4JBug.getDataDir() + proj + "_" + id;
            BugBuilder.getDiffInfo(repository, bug.get(6), bugInduingCommit, "inducing", fileDir);
            BugBuilder.getDiffInfo(repository, bug.get(4), bugFixingCommit, "fixing", fileDir);
        }

    }


    public static void getDiffInfos(String[] args) {//
        String filePath = "src/test/resources/Another.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, false);
        String bugInfoDir = "src/test/resources/BugFixInfo_total.csv";
        StringBuilder stringBuilder = new StringBuilder();//"BugId,Derive,BugName,fixing_commit,fixing_before,inducing_commit,inducing_before\n"
        for (int i = 0; i < d4jinfos.size(); i ++) {
            List<String> bug = d4jinfos.get(i);
            String bug_tag = bug.get(0) + "_" + bug.get(1);
            String bugName = bug_tag + "_buggy";
            String bugInduingCommit = bug.get(2);
//            if (FileUtils.isFileExist(diffDir))
//                continue;
            String workingDir = "../bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(bug.get(0), bug.get(1),  workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            String bugFixingCommit = defects4JBug.getFixingCommit();
            assert repository != null;
            String fixingDiff = gitAccess.diff(repository, bugFixingCommit);
            String inducingDiff = gitAccess.diff(repository, bugInduingCommit);
            String fixingDiffDir = "data/changesInfo/" + bug_tag + "/patches/fixing.diff";
            String inducingDiffDir = "data/changesInfo/" + bug_tag + "/patches/inducing.diff";
            String changesInfoDir = "data/changesInfo/" + bug_tag + "/info.txt";
            if (fixingDiff != null && inducingDiff != null) {
                BugFixCommit bugFixCommit = gitAccess.getBugFixCommit(bugName, String.valueOf(i),
                                            repository, bugInduingCommit, bugFixingCommit);
                FileUtils.writeToFile(bugFixCommit.toString(), changesInfoDir, false);
                FileUtils.writeToFile(fixingDiff, fixingDiffDir, false);
                FileUtils.writeToFile(inducingDiff, inducingDiffDir, false);
                String buginfo = i + "," + "defects4j" +
                        "," + bugName +
                        "," + bugFixingCommit +
                        "," + gitAccess.getCommit(repository, bugFixingCommit).getParent(0).getName() +
                        "," + bugInduingCommit +
                        "," + gitAccess.getCommit(repository, bugInduingCommit).getParent(0).getName() + "\n";
                stringBuilder.append(buginfo);
            }
        }
        FileUtils.writeToFile(stringBuilder.toString(), bugInfoDir, true);

    }

}
