package root.attempt;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleLogger;
import root.bean.BugFixCommit;
import root.benchmarks.Defects4JBug;
import org.eclipse.jgit.lib.Repository;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Defects4jBugsMain implements GitAccess {

    private static final Logger logger = LoggerFactory.getLogger(Defects4jBugsMain.class);

    /**
     *
     * @param args [0]:failing_tests store directory
     */
    public static void main(String[] args) {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        List<String> succeed = new ArrayList<>();
        Map<String, String[]> filter = new HashMap<>();
        logger.info("Starting find original commit...");
        //original wrong
        filter.put("Closure", new String[]{"12","19","30","52","59","60","66","68","75","76","80","82","85","90","99","118","131"});
        filter.put("Math", new String[]{"12","13"});
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
            try {
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "tmp/bugs/" + bugName);
                Repository repository = defects4JBug.getGitRepository("b");
                res = defects4JBug.switchAndTest(repository, bug.get(6), "original");
                if (!res) {
                    logger.info("---------- " + bugName + " failed in initial test.");
                    continue;
                }
                Map<String, String> properties = defects4JBug.getProperties("/defects4j.build.properties");
                String failing_tests_path = defects4JBug.getWorkingDir() + "/failing_tests";
                defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp -r ../../changesInfo/" + proj + "_" + id + "/cleaned/inducing/ ./"}, defects4JBug.getWorkingDir(), 300, null);
                List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repository, false);
                int i = 1;
                for (; i < revsWalkOfAll.size(); i++) {
                    String commit = revsWalkOfAll.get(i).getName();
                    res = defects4JBug.switchAndTest(repository, commit, "original");
                    if (!res) {
                        logger.info("---------- " + bugName + " failed in continuous test.");
                        continue;
                    }
                    properties = defects4JBug.getProperties("/defects4j.build.properties");
                    failing_tests_path = defects4JBug.getWorkingDir() + "/failing_tests";
                    defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
                    res = defects4JBug.test();
                    List<String> failing_tests = FileUtils.readEachLine(defects4JBug.getWorkingDir() + "/failing_tests");
                    if (res && (failing_tests.isEmpty())) {
                        FileUtils.writeToFile(bugName + "," + commit + "\n", args[0] + "/bug_original_commits", true);
                        break;
                    } else {
                        FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp " + defects4JBug.getWorkingDir() + "/failing_tests" + " " + args[0] + "/" + proj + "/failing_tests/" + commit}, defects4JBug.getWorkingDir(), 300, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
            }
            logger.info("Finished processing " + bugName + "...");
        }
        logger.info("Starting find inducing commit...");
        //inducing wrong
        filter.put("Closure", new String[]{"33","107","114","125","130","133"});
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
            try {
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "tmp/bugs/" + bugName);
                Repository repository = defects4JBug.getGitRepository("b");
                res = defects4JBug.switchAndTest(repository, bug.get(6), "original");
                if (!res) {
                    logger.info("---------- " + bugName + " failed in initial test.");
                    continue;
                }
                Map<String, String> properties = defects4JBug.getProperties("/defects4j.build.properties");
                String failing_tests_path = defects4JBug.getWorkingDir() + "/failing_tests";
                List<String> failing_tests = FileUtils.readEachLine(failing_tests_path);
                int size = 0;
                if (!failing_tests.isEmpty()) {
                    size = (int) failing_tests.stream().filter(line -> line.startsWith("---")).count();
                }
                defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp -r ../../changesInfo/" + proj + "_" + id + "/cleaned/inducing/ ./"}, defects4JBug.getWorkingDir(), 300, null);
                List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repository, true);
                int i = 1;
                for (; i < revsWalkOfAll.size(); i++) {
                    String commit = revsWalkOfAll.get(i).getName();
                    res = defects4JBug.switchAndTest(repository, commit, "inducing");
                    if (!res) {
                        logger.info("---------- " + bugName + " failed in continuous test.");
                        continue;
                    }
                    properties = defects4JBug.getProperties("/defects4j.build.properties");
                    failing_tests_path = defects4JBug.getWorkingDir() + "/failing_tests";
                    defects4JBug.rmBrokenTests(failing_tests_path, defects4JBug.getWorkingDir() + "/" + properties.get("test.dir"));
                    res = defects4JBug.test();
                    failing_tests = FileUtils.readEachLine(defects4JBug.getWorkingDir() + "/failing_tests");
                    if (res && (!failing_tests.isEmpty())) {
                        FileUtils.writeToFile(bugName + "," + commit + "\n", args[0] + "/bug_inducing_commits", true);
                        break;
                    } else {
                        FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp " + defects4JBug.getWorkingDir() + "/failing_tests" + " " + args[0] + "/" + proj + "/failing_tests/" + commit}, defects4JBug.getWorkingDir(), 300, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
            }
            logger.info("Finished processing " + bugName + "...");
        }
    }


    public static void getsucceed(String[] args) {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
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
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "tmp/bugs/" + bugName, bug.get(3), bug.get(4), bug.get(5), bug.get(6));
                Repository repo = defects4JBug.getGitRepository("b");
                boolean res = defects4JBug.switchAndTag(repo, defects4JBug.getInducingCommit(), "inducing", "CI_INDUCING_COMPILABLE");
                if (!res) {
                    failed.add(bugName);
                }
            }
        }
        System.out.println(failed.size());
        System.out.println(failed);
    }

    public static void getdiffs(String[] args) {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        for (List<String> bug :d4jinfos) {
            String bugName = bug.get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            //                if (Integer.parseInt(id) >= 11)
            //                    continue;
            String bugFixingCommit = bug.get(3);
            String bugInduingCommit = bug.get(5);
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, "tmp/bugs/" + bugName);
            Repository repository = defects4JBug.getGitRepository("b");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bug.get(4), bug.get(6)
                    , "tmp/changesInfo/" + proj + "_" + id + "/properties/mappings/b2o");
        }
    }

    public static void checkoutAndTest(String[] args) {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        Set<String> projs = d4jinfos.stream().map(o -> o.get(2).split("_")[0]).collect(Collectors.toSet());
        List<String> failed = new ArrayList<>();
        for (String proj :projs) {
//            if (!proj.equals("Lang"))
//                continue;
            for (List<String> bug : d4jinfos.stream().filter(o -> o.get(2).split("_")[0].equals(proj)).collect(Collectors.toList())) {
                String bugName = bug.get(2);
                String id = bugName.split("_")[1];
                if (proj.equals("Closure") && Integer.parseInt(id) < 17)
                    continue;
                String bugInduingCommit = bug.get(5);
                String bugOriginal = bug.get(6);
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "tmp/bugs/" + bugName);
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

    public static void rewriteD4jInfo(String[] args) {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
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
        }
    }


    public static void getChangeInfo(String[] args) {
        String filePath = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        for (List<String> bug : d4jinfos) {
            String bugName = bug.get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bugInduingCommit = bug.get(5);
            String workingDir = "tmp/bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            String bugFixingCommit = bug.get(3);
            defects4JBug.getDiffInfo(repository, bug.get(6), bugInduingCommit, "inducing");
            defects4JBug.getDiffInfo(repository, bug.get(4), bugFixingCommit, "fixing");
        }

    }


    public static void getDiffInfos(String[] args) {
        String filePath = "src/test/resources/Defects4J.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        for (int i = 0; i < d4jinfos.size(); i ++) {
            List<String> bug = d4jinfos.get(i);
            if (bug.get(0).equals("Chart"))
                continue;
            String bugName = bug.get(0) + "_" + bug.get(1) + "_buggy";
            String bugInduingCommit = bug.get(2);
//            if (FileUtils.isFileExist(diffDir))
//                continue;
            String workingDir = "tmp/bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(bug.get(0), bug.get(1),  workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            String bugFixingCommit = Objects.requireNonNull(defects4JBug.getFixedCommit(repository)).getName();
            assert repository != null;
            String fixingDiff = gitAccess.diff(repository, bugFixingCommit);
            String inducingDiff = gitAccess.diff(repository, bugInduingCommit);
            String fixingDiffDir = "tmp/changesInfo/" + bugName + "/patches/fixing.diff";
            String inducingDiffDir = "tmp/changesInfo/" + bugName + "/patches/inducing.diff";
            String bugInfoDir = "src/test/resources/BugFixInfo.csv";
            String changesInfoDir = "tmp/changesInfo/" + bugName + "/info.txt";
            StringBuilder stringBuilder = new StringBuilder("BugId,Derive,BugName,fixing_commit,fixing_before,inducing_commit,inducing_before\n");
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
            FileUtils.writeToFile(stringBuilder.toString(), bugInfoDir, false);
        }

    }

}
