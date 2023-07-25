package root.analysis;

import org.eclipse.jgit.lib.Repository;
import root.bean.BugFixCommit;
import root.benchmarks.Defects4JBug;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BugInfoExtractor implements GitAccess {

    public static void main(String[] args) {
        String filePath = "src/test/resources/Another.csv";//proj,id,inducing_commit
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        String bugInfo = "src/test/resources/BugFixInfo.csv";
        List<List<String>> infos = FileUtils.readCsv(bugInfo, true);
        List<String> bugNames = infos.stream().map(bug -> bug.get(2)).collect(Collectors.toList());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < d4jinfos.size(); i ++) {
            List<String> bug = d4jinfos.get(i);
            String proj = bug.get(0);
            String id = bug.get(1);
            String bug_tag = proj + "_" + id;
            String bugName = bug_tag + "_buggy";
            String workingDir = "../bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(proj, id,  workingDir);
            Repository repository = defects4JBug.getGitRepository("b");

            String bugFixingCommit = defects4JBug.getFixingCommit();
            String bugInduingCommit = bug.get(2);
            if (bugNames.contains(bugName)) {
                List<List<String>> collect = infos.stream().filter(b -> b.get(2).equals(bugName)).collect(Collectors.toList());
                assert collect.size() == 1;
                String inducing = collect.get(0).get(5);
//                if (inducing.equals(bugInduingCommit))
//                    continue;
            }

            // get diff infos
            String fixingDiff = gitAccess.diff(repository, bugFixingCommit);
            String inducingDiff = gitAccess.diff(repository, bugInduingCommit);
            String fixingDiffDir = "tmp/changesInfo/" + bug_tag + "/patches/fixing.diff";
            String inducingDiffDir = "tmp/changesInfo/" + bug_tag + "/patches/inducing.diff";
            String changesInfoDir = "tmp/changesInfo/" + bug_tag + "/info.txt";
            String bugBuggyCommit = gitAccess.getCommit(repository, bugFixingCommit).getParent(0).getName();
            String bugOriginalCommit = gitAccess.getCommit(repository, bugInduingCommit).getParent(0).getName();
            if (fixingDiff != null && inducingDiff != null) {
                BugFixCommit bugFixCommit = gitAccess.getBugFixCommit(bugName, String.valueOf(i),
                        repository, bugInduingCommit, bugFixingCommit);
                FileUtils.writeToFile(bugFixCommit.toString(), changesInfoDir, false);
                FileUtils.writeToFile(fixingDiff, fixingDiffDir, false);
                FileUtils.writeToFile(inducingDiff, inducingDiffDir, false);
                String buginfo = i + "," + "defects4j" +
                        "," + bugName +
                        "," + bugFixingCommit +
                        "," + bugBuggyCommit +
                        "," + bugInduingCommit +
                        "," + bugOriginalCommit + "\n";
                stringBuilder.append(buginfo);
            }
            FileUtils.writeToFile(stringBuilder.toString(), bugInfo, true);

            //get modified changes
            defects4JBug.getDiffInfo(repository, bugOriginalCommit, bugInduingCommit, "inducing");
            defects4JBug.getDiffInfo(repository, bugBuggyCommit, bugFixingCommit, "fixing");

            //get mappings: f2i,i2o,f2b
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugFixingCommit, bugInduingCommit
                    , "tmp/changesInfo/" + proj + "_" + id + "/properties/mappings/f2i");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugInduingCommit, bugOriginalCommit
                    , "tmp/changesInfo/" + proj + "_" + id + "/properties/mappings/i2o");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugFixingCommit, bugBuggyCommit
                    , "tmp/changesInfo/" + proj + "_" + id + "/properties/mappings/f2b");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugFixingCommit, bugOriginalCommit
                    , "tmp/changesInfo/" + proj + "_" + id + "/properties/mappings/f2o");

            //get defects4j patches
            String patches = "tmp/changesInfo/" + proj + "_" + id + "/patches/";
            String srcPatch = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/patches/" + id + ".src.patch";
            String testPatch = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/patches/" + id + ".test.patch";
            FileUtils.copy(new File(srcPatch), new File(patches));
            FileUtils.copy(new File(testPatch), new File(patches));

            //get sources of modified classes
            try {
                String mappingFile = "tmp/changesInfo/" + proj + "_" + id + "/properties/mappings/";
                getMappingFiles(defects4JBug, mappingFile + "f2b", bugFixingCommit, "fixing");
                getMappingFiles(defects4JBug, mappingFile + "f2b", bugBuggyCommit, "buggy");
                getMappingFiles(defects4JBug, mappingFile + "f2i", bugInduingCommit, "inducing");
                getMappingFiles(defects4JBug, mappingFile + "f2o", bugOriginalCommit, "original");
            } catch (Exception e) {
                System.err.println(bugName);
                e.printStackTrace();
            }
        }
    }

    static boolean getMappingFiles(Defects4JBug defects4JBug, String mappingFile, String commitId, String version) {
        String modifiedClasses = "tmp/changesInfo/" + defects4JBug.getProj() + "_" + defects4JBug.getId() + "/properties/modified_classes/";
        String srcClasses = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + defects4JBug.getProj() + "/modified_classes/" + defects4JBug.getId() + ".src";
        String testMethods = "/home/liumengjiao/Desktop/vbaprinfo/d4j_bug_info/failed_tests/" + defects4JBug.getProj().toLowerCase() + "/" + defects4JBug.getId() + ".txt";
        List<List<String>> b2iSrc = gitAccess.getF2i(mappingFile, FileUtils.readEachLine(srcClasses));
        List<List<String>> b2iTest = gitAccess.getF2i(mappingFile, FileUtils.readEachLine(testMethods));
        boolean checkoutf = gitAccess.checkoutf(defects4JBug.getWorkingDir(), commitId);
        if (!checkoutf) {
            return false;
        }
        for (List<String> b2i : b2iSrc) {
            FileUtils.copy(new File(defects4JBug.getWorkingDir() + "/" + b2i.get(2)), new File(modifiedClasses + version + "/" + b2i.get(2)));
        }
        for (List<String> b2i : b2iTest) {
            FileUtils.copy(new File(defects4JBug.getWorkingDir() + "/" + b2i.get(2)), new File(modifiedClasses + version + "/" + b2i.get(2)));
        }
//                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "diff -r -u original/ buggy/ > " + "../../patches/o2b.diff 2>&1"}, modifiedClasses, 300, null);
        return true;
    }
}
